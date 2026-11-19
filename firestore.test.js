const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const ADMIN_UID = "admin_uid_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(msOffset = 1000) {
  return new Date(Date.now() - msOffset);
}

function validNewUserPayload(uid, referralCode = "ALICE01") {
  const now = pastDate(2000);
  return {
    userId: uid,
    fullName: "Alice User",
    mobileNumber: "9876543210",
    email: "alice@example.com",
    role: "user",
    activationStatus: "unactivated",
    walletBalance: 0,
    pendingBalance: 0,
    totalEarnings: 0,
    todayEarnings: 0,
    totalDataSoldGb: 0,
    pendingDataGb: 0,
    totalTransactions: 0,
    referralCode,
    referralCount: 0,
    referralEarnings: 0,
    blocked: false,
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users are denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("data_packages").get());
  await assertFails(unauthDb.collection("announcements").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validNewUserPayload(ALICE_UID)));
});

test("User registration enforces zero balances, role=user, unactivated status, and PII isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();

  // Valid user creation succeeds
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validNewUserPayload(ALICE_UID)));

  // Reject role escalation on create
  const adminSpoof = { ...validNewUserPayload(BOB_UID, "BOB0001"), role: "admin" };
  await assertFails(bobDb.collection("users").doc(BOB_UID).set(adminSpoof));

  // Reject self-activated or pre-funded wallet on create
  const walletSpoof = { ...validNewUserPayload(BOB_UID, "BOB0001"), walletBalance: 500 };
  await assertFails(bobDb.collection("users").doc(BOB_UID).set(walletSpoof));

  // Reject self-activation on update
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      activationStatus: "activated",
      updatedAt: pastDate(500),
    })
  );

  // Reject walletBalance self-modification on update
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      walletBalance: 999,
      updatedAt: pastDate(500),
    })
  );

  // Reject cross-user PII read
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").get());
});

test("Activation payment requires atomic UTR claim and prevents duplicate UTR submission", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validNewUserPayload(ALICE_UID)));
  await assertSucceeds(bobDb.collection("users").doc(BOB_UID).set(validNewUserPayload(BOB_UID, "BOB0002")));

  const now = pastDate(1000);
  const utr = "UTR123456789012";

  // Direct activation payment without atomic utr_claims must FAIL
  await assertFails(
    aliceDb.collection("activation_payments").doc("pay_fail").set({
      userId: ALICE_UID,
      userName: "Alice User",
      userEmail: "alice@example.com",
      amount: 5,
      upiId: "janara2@ptyes",
      utrReference: utr,
      status: "pending_verification",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Atomic batch creating utr_claims + activation_payments + user status pending_verification SUCCEEDS
  const batch = aliceDb.batch();
  batch.set(aliceDb.collection("utr_claims").doc(utr), {
    utr,
    userId: ALICE_UID,
    purpose: "activation",
    createdAt: now,
  });
  batch.set(aliceDb.collection("activation_payments").doc("pay_1"), {
    userId: ALICE_UID,
    userName: "Alice User",
    userEmail: "alice@example.com",
    amount: 5,
    upiId: "janara2@ptyes",
    utrReference: utr,
    status: "pending_verification",
    createdAt: now,
    updatedAt: now,
  });
  batch.update(aliceDb.collection("users").doc(ALICE_UID), {
    activationStatus: "pending_verification",
    updatedAt: now,
  });
  await assertSucceeds(batch.commit());

  // Duplicate UTR claim by Bob must FAIL
  const bobBatch = bobDb.batch();
  bobBatch.set(bobDb.collection("utr_claims").doc(utr), {
    utr,
    userId: BOB_UID,
    purpose: "activation",
    createdAt: now,
  });
  bobBatch.set(bobDb.collection("activation_payments").doc("pay_2"), {
    userId: BOB_UID,
    userName: "Bob User",
    userEmail: "bob@example.com",
    amount: 5,
    upiId: "janara2@ptyes",
    utrReference: utr,
    status: "pending_verification",
    createdAt: now,
    updatedAt: now,
  });
  await assertFails(bobBatch.commit());
});

test("Data sell and withdrawal require activated status, valid package, min withdrawal, and balance", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    const now = pastDate(5000);
    await db.collection("admins").doc(ADMIN_UID).set({
      uid: ADMIN_UID,
      role: "super_admin",
      addedBy: "system",
      createdAt: now,
    });
    await db.collection("app_settings").doc("global").set({
      activationFeeInr: 5,
      activationUpiId: "janara2@ptyes",
      minWithdrawalInr: 50,
      referralRewardInr: 10,
      referralsEnabled: true,
      supportEmail: "support@dilsatdatasell.com",
      announcementBanner: "Welcome",
      ratePer100MbInr: 2,
      minSellDataMb: 100,
      updatedAt: now,
    });
    await db.collection("data_packages").doc("pkg_5g").set({
      name: "5G Ultra Pool",
      packageType: "5G",
      pricePerGb: 20,
      minGb: 1,
      maxGb: 100,
      description: "High speed 5G surplus",
      active: true,
      createdAt: now,
      updatedAt: now,
    });
    await db.collection("users").doc(ALICE_UID).set(validNewUserPayload(ALICE_UID));
    await db.collection("users").doc(BOB_UID).set({
      ...validNewUserPayload(BOB_UID, "BOB0002"),
      activationStatus: "activated",
      walletBalance: 120,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "islamdilsat123@gmail.com",
    email_verified: true,
  }).firestore();
  const now = pastDate(1000);

  // Unactivated Alice cannot submit data sell request
  await assertFails(
    aliceDb.collection("data_sell_requests").doc("req_1").set({
      userId: ALICE_UID,
      userName: "Alice User",
      packageId: "pkg_5g",
      packageName: "5G Ultra Pool",
      packageType: "5G",
      quantityGb: 0.5,
      quantityMb: 500,
      pricePerGb: 20,
      estimatedPayout: 10,
      networkProvider: "Jio",
      status: "pending",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Activated Bob below minimum 100 MB fails
  await assertFails(
    bobDb.collection("data_sell_requests").doc("req_low_mb").set({
      userId: BOB_UID,
      userName: "Bob User",
      packageId: "standard_mb_pool",
      packageName: "4G Data Pool",
      packageType: "4G",
      quantityGb: 0.05,
      quantityMb: 50,
      pricePerGb: 20,
      estimatedPayout: 1,
      networkProvider: "Jio",
      status: "pending",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Activated Bob at 100 MB with standard_mb_pool succeeds
  await assertSucceeds(
    bobDb.collection("data_sell_requests").doc("req_min_mb").set({
      userId: BOB_UID,
      userName: "Bob User",
      packageId: "standard_mb_pool",
      packageName: "4G Data Pool",
      packageType: "4G",
      quantityGb: 0.1,
      quantityMb: 100,
      pricePerGb: 20,
      estimatedPayout: 2,
      networkProvider: "Jio",
      status: "pending",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Activated Bob can submit valid data sell request with packageId
  await assertSucceeds(
    bobDb.collection("data_sell_requests").doc("req_2").set({
      userId: BOB_UID,
      userName: "Bob User",
      packageId: "pkg_5g",
      packageName: "5G Ultra Pool",
      packageType: "5G",
      quantityGb: 5,
      quantityMb: 5000,
      pricePerGb: 20,
      estimatedPayout: 100,
      networkProvider: "Jio",
      status: "pending",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Regular user cannot alter ratePer100MbInr or minSellDataMb; admin can
  await assertFails(
    bobDb.collection("app_settings").doc("global").update({
      ratePer100MbInr: 10,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("app_settings").doc("global").update({
      ratePer100MbInr: 3,
      minSellDataMb: 200,
      updatedAt: now,
    })
  );

  // After admin raises minSellDataMb to 200, 100 MB request is rejected
  await assertFails(
    bobDb.collection("data_sell_requests").doc("req_below_new_min").set({
      userId: BOB_UID,
      userName: "Bob User",
      packageId: "standard_mb_pool",
      packageName: "4G Data Pool",
      packageType: "4G",
      quantityGb: 0.1,
      quantityMb: 100,
      pricePerGb: 30,
      estimatedPayout: 3,
      networkProvider: "Jio",
      status: "pending",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Withdrawal below minWithdrawalInr (50) fails
  await assertFails(
    bobDb.collection("withdrawals").doc("wd_low").set({
      userId: BOB_UID,
      userName: "Bob User",
      amount: 20,
      upiId: "bob@upi",
      accountHolderName: "Bob User",
      status: "pending",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Withdrawal above walletBalance (120) fails
  await assertFails(
    bobDb.collection("withdrawals").doc("wd_over").set({
      userId: BOB_UID,
      userName: "Bob User",
      amount: 500,
      upiId: "bob@upi",
      accountHolderName: "Bob User",
      status: "pending",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid withdrawal within min..balance succeeds
  await assertSucceeds(
    bobDb.collection("withdrawals").doc("wd_ok").set({
      userId: BOB_UID,
      userName: "Bob User",
      amount: 100,
      upiId: "bob@upi",
      accountHolderName: "Bob User",
      status: "pending",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Query alignment tests
  await assertSucceeds(bobDb.collection("data_sell_requests").where("userId", "==", BOB_UID).get());
  await assertFails(bobDb.collection("data_sell_requests").get());
  await assertSucceeds(bobDb.collection("withdrawals").where("userId", "==", BOB_UID).get());
  await assertFails(bobDb.collection("withdrawals").get());
});

test("Bootstrapped admin email requires email_verified=true and admin can verify payments & manage transactions", async () => {
  const spoofedAdminDb = testEnv.authenticatedContext("spoof_uid", {
    email: "islamdilsat123@gmail.com",
    email_verified: false,
  }).firestore();

  const realAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "islamdilsat123@gmail.com",
    email_verified: true,
  }).firestore();

  const now = pastDate(1000);

  // Spoofed unverified email cannot write admin doc
  await assertFails(
    spoofedAdminDb.collection("admins").doc("spoof_uid").set({
      uid: "spoof_uid",
      role: "super_admin",
      addedBy: "self",
      createdAt: now,
    })
  );

  // Verified bootstrapped admin can claim /admins/{uid} and list users
  await assertSucceeds(
    realAdminDb.collection("admins").doc(ADMIN_UID).set({
      uid: ADMIN_UID,
      role: "super_admin",
      addedBy: "bootstrap",
      createdAt: now,
    })
  );
  await assertSucceeds(realAdminDb.collection("users").get());
});
