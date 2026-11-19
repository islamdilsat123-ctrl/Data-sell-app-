package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.firebase.AdminRepository
import com.example.data.firebase.AppRepository
import com.example.data.firebase.MarketplaceRepository
import com.example.data.firebase.attemptAutoSignIn
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.signOutSafely
import com.example.data.model.UiState
import com.example.data.preferences.ThemeMode
import com.example.ui.components.FintechBackdrop
import com.example.ui.components.FintechCard
import com.example.ui.screens.ActivationPaymentScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SellDataScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.LocalDilsatColors
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

enum class MainTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    SELL_DATA("sell_data", R.string.nav_sell_data, Icons.Filled.Bolt, Icons.Outlined.Bolt),
    WALLET("wallet", R.string.nav_wallet, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    HISTORY("history", R.string.nav_history, Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
    PROFILE("profile", R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person)
}

enum class SubRoute {
    NONE, NOTIFICATIONS, SUPPORT, ADMIN
}

@Composable
fun AppNavigation(
    themeMode: ThemeMode,
    isDarkTheme: Boolean,
    onSelectTheme: (ThemeMode) -> Unit,
    auth: FirebaseAuth = Firebase.auth
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var showSplash by remember { mutableStateOf(true) }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            AuthenticatedAppShell(
                firebaseUser = user,
                themeMode = themeMode,
                isDarkTheme = isDarkTheme,
                onSelectTheme = onSelectTheme,
                onSignOut = {
                    signOutSafely(credentialManager, scope)
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedAppShell(
    firebaseUser: FirebaseUser,
    themeMode: ThemeMode,
    isDarkTheme: Boolean,
    onSelectTheme: (ThemeMode) -> Unit,
    onSignOut: () -> Unit
) {
    val uid = firebaseUser.uid
    val mainViewModel: MainViewModel = viewModel(
        key = uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MainViewModel(
                    appRepo = AppRepository(db),
                    marketRepo = MarketplaceRepository(db),
                    currentUserId = uid
                )
            }
        }
    )

    val ext = LocalDilsatColors.current
    val snackbarHostState = remember { SnackbarHostState() }

    val profileState by mainViewModel.userProfileState.collectAsStateWithLifecycle()
    val settings by mainViewModel.appSettings.collectAsStateWithLifecycle()
    val isAdmin by mainViewModel.isAdmin.collectAsStateWithLifecycle()
    val activationPaymentsState by mainViewModel.activationPaymentsState.collectAsStateWithLifecycle()
    val packagesState by mainViewModel.dataPackagesState.collectAsStateWithLifecycle()
    val sellRequestsState by mainViewModel.sellRequestsState.collectAsStateWithLifecycle()
    val withdrawalsState by mainViewModel.withdrawalsState.collectAsStateWithLifecycle()
    val transactionsState by mainViewModel.transactionsState.collectAsStateWithLifecycle()
    val referralsState by mainViewModel.referralsState.collectAsStateWithLifecycle()
    val notificationsState by mainViewModel.notificationsState.collectAsStateWithLifecycle()
    val announcements by mainViewModel.announcements.collectAsStateWithLifecycle()
    val supportTicketsState by mainViewModel.supportTicketsState.collectAsStateWithLifecycle()
    val isSubmitting by mainViewModel.isSubmitting.collectAsStateWithLifecycle()
    val feedback by mainViewModel.feedbackMessage.collectAsStateWithLifecycle()

    LaunchedEffect(feedback) {
        feedback?.let {
            snackbarHostState.showSnackbar(it)
            mainViewModel.clearFeedback()
        }
    }

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var subRoute by remember { mutableStateOf(SubRoute.NONE) }
    // Track whether the user watched the live transition from unactivated -> activated so we show the Lottie success screen first
    var sawUnactivatedSession by remember { mutableStateOf(false) }
    var unlockedAfterSuccess by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val pState = profileState) {
            is UiState.Loading -> {
                FintechBackdrop {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ext.neonGreen)
                    }
                }
            }
            is UiState.Error -> {
                FintechBackdrop {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FintechCard(accentBorder = ext.statusError, modifier = Modifier.widthIn(max = 460.dp)) {
                            Text("Unable to load account", style = MaterialTheme.typography.titleLarge, color = ext.statusError)
                            Spacer(Modifier.height(6.dp))
                            Text(pState.message, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(14.dp))
                            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                                Text("Sign Out")
                            }
                        }
                    }
                }
            }
            is UiState.Success -> {
                val profile = pState.data
                if (profile == null) {
                    RegistrationScreen(
                        firebaseUser = firebaseUser,
                        viewModel = mainViewModel,
                        isSubmitting = isSubmitting,
                        onSignOut = onSignOut
                    )
                } else {
                    if (!profile.isActivated) {
                        sawUnactivatedSession = true
                        unlockedAfterSuccess = false
                    }

                    // Overlay Sub-Routes (Support, Notifications, Admin Panel)
                    if (subRoute != SubRoute.NONE) {
                        BackHandler { subRoute = SubRoute.NONE }
                        when (subRoute) {
                            SubRoute.NOTIFICATIONS -> NotificationsScreen(
                                notificationsState = notificationsState,
                                announcements = announcements,
                                onMarkRead = { mainViewModel.markNotificationRead(it) },
                                onBack = { subRoute = SubRoute.NONE }
                            )
                            SubRoute.SUPPORT -> SupportScreen(
                                profile = profile,
                                settings = settings,
                                ticketsState = supportTicketsState,
                                isSubmitting = isSubmitting,
                                viewModel = mainViewModel,
                                onBack = { subRoute = SubRoute.NONE }
                            )
                            SubRoute.ADMIN -> {
                                if (isAdmin) {
                                    val adminVm: AdminViewModel = viewModel(
                                        key = "admin_$uid",
                                        factory = viewModelFactory {
                                            initializer {
                                                val app = checkNotNull(this[APPLICATION_KEY])
                                                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                                                AdminViewModel(
                                                    adminRepo = AdminRepository(db),
                                                    marketRepo = MarketplaceRepository(db)
                                                )
                                            }
                                        }
                                    )
                                    AdminPanelScreen(
                                        adminViewModel = adminVm,
                                        settings = settings,
                                        onBack = { subRoute = SubRoute.NONE }
                                    )
                                } else {
                                    subRoute = SubRoute.NONE
                                }
                            }
                            SubRoute.NONE -> Unit
                        }
                    } else if (profile.blocked) {
                        FintechBackdrop {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .safeDrawingPadding()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                FintechCard(accentBorder = ext.statusError, modifier = Modifier.widthIn(max = 460.dp)) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Icon(Icons.Filled.Block, contentDescription = "Blocked", tint = ext.statusError)
                                        Spacer(Modifier.height(10.dp))
                                        Text("Account Suspended", style = MaterialTheme.typography.headlineMedium, color = ext.statusError)
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "Your account has been restricted by security moderators. Contact support for review.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Button(onClick = { subRoute = SubRoute.SUPPORT }, modifier = Modifier.fillMaxWidth()) {
                                            Text("Contact Support")
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                                            Text("Sign Out")
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!profile.isActivated || (sawUnactivatedSession && !unlockedAfterSuccess)) {
                        val latestPay = (activationPaymentsState as? UiState.Success)?.data?.firstOrNull()
                        ActivationPaymentScreen(
                            profile = profile,
                            settings = settings,
                            latestPayment = latestPay,
                            isSubmitting = isSubmitting,
                            isAdmin = isAdmin,
                            viewModel = mainViewModel,
                            onEnterDashboard = { unlockedAfterSuccess = true },
                            onOpenSupport = { subRoute = SubRoute.SUPPORT },
                            onOpenAdminPanel = { subRoute = SubRoute.ADMIN },
                            onSignOut = onSignOut
                        )
                    } else {
                        // Main Unlocked Marketplace & Fintech Shell (Adaptive Bottom Bar / Nav Rail)
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val useRail = maxWidth >= 600.dp
                            Scaffold(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                                bottomBar = {
                                    if (!useRail) {
                                        NavigationBar(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ) {
                                            MainTab.entries.forEach { tab ->
                                                val selected = currentTab == tab
                                                NavigationBarItem(
                                                    selected = selected,
                                                    onClick = { currentTab = tab },
                                                    icon = {
                                                        Icon(
                                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                            contentDescription = stringResource(tab.labelRes)
                                                        )
                                                    },
                                                    label = { Text(stringResource(tab.labelRes)) },
                                                    modifier = Modifier.testTag("nav_${tab.route}")
                                                )
                                            }
                                        }
                                    }
                                }
                            ) { innerPadding ->
                                FintechBackdrop(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .safeDrawingPadding()
                                    ) {
                                        if (useRail) {
                                            NavigationRail(
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                modifier = Modifier.fillMaxHeight()
                                            ) {
                                                Spacer(Modifier.height(12.dp))
                                                MainTab.entries.forEach { tab ->
                                                    val selected = currentTab == tab
                                                    NavigationRailItem(
                                                        selected = selected,
                                                        onClick = { currentTab = tab },
                                                        icon = {
                                                            Icon(
                                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                                contentDescription = stringResource(tab.labelRes)
                                                            )
                                                        },
                                                        label = { Text(stringResource(tab.labelRes)) },
                                                        modifier = Modifier.testTag("rail_${tab.route}")
                                                    )
                                                }
                                            }
                                        }

                                        AnimatedContent(
                                            targetState = currentTab,
                                            transitionSpec = {
                                                (fadeIn() + slideInHorizontally { it / 16 }) togetherWith
                                                    (fadeOut() + slideOutHorizontally { -it / 16 })
                                            },
                                            label = "tab_transition",
                                            modifier = Modifier.weight(1f)
                                        ) { tab ->
                                            when (tab) {
                                                MainTab.HOME -> DashboardScreen(
                                                    profile = profile,
                                                    settings = settings,
                                                    transactionsState = transactionsState,
                                                    notificationsState = notificationsState,
                                                    isDarkTheme = isDarkTheme,
                                                    onToggleTheme = {
                                                        onSelectTheme(if (isDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK)
                                                    },
                                                    onNavigateSellData = { currentTab = MainTab.SELL_DATA },
                                                    onNavigateWallet = { currentTab = MainTab.WALLET },
                                                    onNavigateHistory = { currentTab = MainTab.HISTORY },
                                                    onNavigateNotifications = { subRoute = SubRoute.NOTIFICATIONS },
                                                    onNavigateProfile = { currentTab = MainTab.PROFILE }
                                                )
                                                MainTab.SELL_DATA -> SellDataScreen(
                                                    profile = profile,
                                                    settings = settings,
                                                    packagesState = packagesState,
                                                    sellRequestsState = sellRequestsState,
                                                    isSubmitting = isSubmitting,
                                                    viewModel = mainViewModel
                                                )
                                                MainTab.WALLET -> WalletScreen(
                                                    profile = profile,
                                                    settings = settings,
                                                    withdrawalsState = withdrawalsState,
                                                    isSubmitting = isSubmitting,
                                                    viewModel = mainViewModel
                                                )
                                                MainTab.HISTORY -> HistoryScreen(
                                                    transactionsState = transactionsState
                                                )
                                                MainTab.PROFILE -> ProfileScreen(
                                                    profile = profile,
                                                    settings = settings,
                                                    referralsState = referralsState,
                                                    isAdmin = isAdmin,
                                                    themeMode = themeMode,
                                                    onSelectTheme = onSelectTheme,
                                                    onOpenNotifications = { subRoute = SubRoute.NOTIFICATIONS },
                                                    onOpenSupport = { subRoute = SubRoute.SUPPORT },
                                                    onOpenAdminPanel = { subRoute = SubRoute.ADMIN },
                                                    onSignOut = onSignOut,
                                                    viewModel = mainViewModel
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(bottom = 68.dp, start = 16.dp, end = 16.dp)
        )
    }
}
