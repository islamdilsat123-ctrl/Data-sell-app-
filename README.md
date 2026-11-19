# Dilsat Data Sell

A full-stack Kotlin & Jetpack Compose Android application for a mobile/broadband surplus data selling marketplace backed by **Firebase Authentication** (Google Sign-In via Jetpack Credential Manager) and **Cloud Firestore**.

---

## Core Features

1. **Animated Splash & Fintech Theme**
   - Dark-first fintech palette (deep navy/black surfaces, neon green `#00F5A0`, electric cyan `#00D9F5`, vivid purple `#8B5CF6`) with Light/Dark/System theme toggle.
   - Custom typography (`Space Grotesk`, `Plus Jakarta Sans`, `JetBrains Mono`) and Lottie animations (`res/raw/lottie_*.json`).
2. **Authentication & Account Registration**
   - Google Sign-In via Jetpack Credential Manager (`GetSignInWithGoogleOption` + silent auto sign-in).
   - User registration (Full Name, Mobile Number, Email, Profile Photo via Android Photo Picker, optional Referral Code).
3. **₹5 One-Time Activation Gate (`janara2@ptyes`)**
   - Deep-link UPI payment (`upi://pay?...`) + built-in QR graphic (`UpiQrGraphic`).
   - Atomic UTR claim (`/utr_claims/{utr}`) prevents duplicate UTR reuse across accounts.
   - Pending verification, Payment Failed (with Retry & Support), and Lottie Success unlock flow.
4. **Data Sell Marketplace & Animated MB Calculator**
   - **Minimum sell quantity:** `100 MB` (admin-configurable).
   - **Default rate:** `₹2 per 100 MB` (`100 MB = ₹2`, `500 MB = ₹10`, `1,000 MB = ₹20`, `5,000 MB = ₹100`, `10,000 MB = ₹200`).
   - Live formula: `Earnings = (Data MB ÷ 100) × Rate`.
   - Automatic button disable and validation warning below `100 MB`.
   - Request tracking with **Pending**, **Approved**, and **Rejected** statuses; wallet is credited only after Admin review & approval.
5. **Dashboard, Wallet, Withdrawals & Transaction History**
   - Live wallet balance, today's & total earnings, total data sold, pending data, and total transactions.
   - UPI withdrawal requests validated against admin-configurable minimum withdrawal limits and available wallet balance.
   - Searchable & filterable transaction ledger with unique `TXN-YYYYMMDD-XXXXXX` IDs.
6. **Referral Program, Notifications & Support**
   - Unique referral code sharing, referral status tracking, and admin-configurable reward amounts.
   - In-app notifications and global announcement banners.
   - Help Center, FAQs, payment issue reporting, and support tickets.
7. **Admin Command Center (RBAC)**
   - Bootstrapped admin verification (`admins` collection + verified email claim in `firestore.rules`).
   - Verify/reject ₹5 activation payments, approve/credit or reject data sell requests, process/pay UPI withdrawals, block/unblock users, send notifications/announcements, manage data packages, configure `ratePer100MbInr` / `minSellDataMb` / `minWithdrawalInr` / `referralRewardInr`, and export CSV transaction reports.

---

## Project Structure

```text
├── app/
│   ├── build.gradle.kts                     # App Gradle config (AGP 9.1.1, Compose, Firebase, Lottie)
│   ├── google-services.json                 # Firebase Android configuration
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/
│       │   ├── MainActivity.kt              # Edge-to-edge activity entry point
│       │   ├── data/
│       │   │   ├── firebase/                # AppRepository, MarketplaceRepository, AdminRepository, Auth helpers
│       │   │   ├── model/Models.kt          # Firestore data models (@Keep, @IgnoreExtraProperties)
│       │   │   └── preferences/             # DataStore theme preferences
│       │   └── ui/
│       │       ├── components/              # DilsatComponents (FintechCard, NeonGradientButton, Lottie, QR)
│       │       ├── navigation/              # Adaptive BottomBar / NavigationRail + auth/activation gates
│       │       ├── screens/                 # 13 Compose screens (Splash, Auth, Activation, Dashboard, SellData, Wallet, History, Profile, Support, Notifications, AdminPanel, AdminTabs)
│       │       ├── theme/                   # Color.kt, Theme.kt, Type.kt
│       │       └── viewmodel/               # MainViewModel, AdminViewModel
│       └── res/
│           ├── font/                        # Local .ttf fonts (space_grotesk, plus_jakarta_sans, jetbrains_mono)
│           ├── raw/                         # Lottie animations
│           └── values/                      # strings.xml, themes.xml, firebase_applet_config.xml
├── firestore.rules                          # Zero-trust Cloud Firestore security rules
├── firebase-blueprint.json                  # Firestore collection schemas & access rules blueprint
├── firestore.test.js                        # Node.js Firestore Rules Unit Tests
├── gradle/libs.versions.toml                # Centralized version catalog
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## Backend & Database Setup (Firebase)

1. **Firestore Database & Auth**
   - Named Firestore database configured in `app/src/main/res/values/firebase_applet_config.xml` (`firestore_database_id`).
   - Google Sign-In configured in `app/google-services.json`.
   - Note: If you re-package or sign with your own keystore in AndroidIDE/Android Studio, add your keystore's **SHA-1** and **SHA-256** fingerprints in **Firebase Console → Project Settings → Your Android App**, then download the updated `google-services.json` into `app/`.
2. **Deploy Security Rules**
   ```bash
   firebase deploy --only firestore:rules
   ```
3. **Run Security Rules Tests Locally**
   ```bash
   firebase emulators:start --only firestore
   FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
   ```
4. **Admin Access**
   - Admin access is controlled by `/admins/{uid}` in Firestore and `firestore.rules`. On first sign-in with the designated admin email, tap **Claim Admin Access** in Profile (or seed `/admins/{uid}` in Firestore Console).

---

## Build Instructions (AndroidIDE & Android Studio)

### Requirements
- **JDK**: Java 17 or Java 21 (bytecode target: Java 11)
- **Android SDK**: `compileSdk = 36` (minor API 1), `minSdk = 24`, `targetSdk = 36`
- **Gradle**: `9.3.1` / Android Gradle Plugin `9.1.1` / Kotlin `2.4.21`

### Building in AndroidIDE (on Android)
1. Extract `Dilsat_Data_Sell_Project.zip` to a folder on your device (e.g. `~/AndroidIDEProjects/DilsatDataSell`).
2. Open the folder in **AndroidIDE**.
3. If your device has limited RAM (4 GB–6 GB), edit `gradle.properties` and reduce `org.gradle.jvmargs`:
   ```properties
   org.gradle.jvmargs=-Xmx1536m -Dfile.encoding=UTF-8
   ```
4. **Debug Keystore Note**:
   - The project includes `debug.keystore.base64` (`storepass = android`, `keypass = android`, `keyAlias = androiddebugkey`).
   - Before building in AndroidIDE, decode `debug.keystore` in the project root using AndroidIDE's terminal:
     ```bash
     base64 -d debug.keystore.base64 > debug.keystore
     ```
   - Alternatively, remove the custom `debugConfig` signing block in `app/build.gradle.kts` to let AndroidIDE sign with your device's default debug keystore (and register that SHA-1 in Firebase Console for Google Sign-In).
5. Tap **Build / Run** (`assembleDebug`) in AndroidIDE to generate `app/build/outputs/apk/debug/app-debug.apk`.

### Building via Command Line / Android Studio
```bash
# Decode debug keystore if not already present
base64 -d debug.keystore.base64 > debug.keystore

# Build Debug APK
gradle :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

---

## Main Dependencies (`gradle/libs.versions.toml`)
- Jetpack Compose BOM `2024.09.00` + Material 3 + Material Icons Extended
- Navigation Compose `2.8.9`
- Lifecycle ViewModel & Runtime Compose `2.8.7`
- DataStore Preferences `1.1.7`
- Firebase BoM `35.1.0` (`firebase-auth`, `firebase-firestore`)
- Jetpack Credential Manager `1.5.0` & Google Identity (`googleid:1.1.1`)
- Coil Compose `2.7.0`
- Lottie Compose `6.6.2`
- Robolectric `4.16.1` & JUnit for unit tests
