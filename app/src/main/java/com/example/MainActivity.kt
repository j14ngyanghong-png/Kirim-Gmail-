package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AdminDashboardScreen
import com.example.ui.AnnouncementsScreen
import com.example.ui.AuthState
import com.example.ui.DashboardScreen
import com.example.ui.ForgotPasswordScreen
import com.example.ui.LoginScreen
import com.example.ui.MainViewModel
import com.example.ui.MaintenanceScreen
import com.example.ui.ProfileScreen
import com.example.ui.RegisterScreen
import com.example.ui.ScreenTab
import com.example.ui.SetorGmailScreen
import com.example.ui.WithdrawalScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val deposits by viewModel.userDeposits.collectAsStateWithLifecycle()
    val withdrawals by viewModel.userWithdrawals.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()

    val allDeposits by viewModel.allDeposits.collectAsStateWithLifecycle()
    val allWithdrawals by viewModel.allWithdrawals.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val pendingDepositCount by viewModel.pendingDepositCount.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Check if server is in maintenance mode and user is not admin
    val isMaintenance = appConfig?.isMaintenanceMode == true && currentUser?.role != "ADMIN"

    if (isMaintenance && authState == AuthState.LOGGED_IN) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            MaintenanceScreen(
                message = appConfig?.maintenanceMessage ?: "Server sedang dalam pemeliharaan.",
                onRefreshStatus = {
                    viewModel.quickLogin(asAdmin = false)
                },
                onAdminBypass = {
                    viewModel.quickLogin(asAdmin = true)
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    when (authState) {
        AuthState.LOGIN_SCREEN -> {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                LoginScreen(
                    onLoginClick = { email, pass -> viewModel.login(email, pass) },
                    onGoogleLoginClick = { email, name -> viewModel.loginWithGoogle(email, name) },
                    onRegisterClick = { viewModel.setAuthState(AuthState.REGISTER_SCREEN) },
                    onForgotPasswordClick = { viewModel.setAuthState(AuthState.FORGOT_PASSWORD_SCREEN) },
                    onQuickUserLogin = { viewModel.quickLogin(asAdmin = false) },
                    onQuickAdminLogin = { viewModel.quickLogin(asAdmin = true) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        AuthState.REGISTER_SCREEN -> {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                RegisterScreen(
                    onRegisterSubmit = { u, e, ph, p, ref ->
                        viewModel.register(u, e, ph, p, ref)
                    },
                    onGoogleLoginClick = { email, name -> viewModel.loginWithGoogle(email, name) },
                    onBackToLogin = { viewModel.setAuthState(AuthState.LOGIN_SCREEN) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        AuthState.FORGOT_PASSWORD_SCREEN -> {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                ForgotPasswordScreen(
                    onResetSubmit = { target, newPass ->
                        viewModel.resetPassword(target, newPass)
                    },
                    onBackToLogin = { viewModel.setAuthState(AuthState.LOGIN_SCREEN) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        AuthState.LOGGED_IN -> {
            Scaffold(
                bottomBar = {
                    AppBottomNavigation(
                        currentTab = activeTab,
                        onTabSelected = { viewModel.setTab(it) },
                        isAdmin = currentUser?.role == "ADMIN" || activeTab == ScreenTab.ADMIN,
                        pendingDepositCount = pendingDepositCount
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TabTransition"
                    ) { targetTab ->
                        when (targetTab) {
                            ScreenTab.HOME -> {
                                DashboardScreen(
                                    user = currentUser,
                                    config = appConfig,
                                    deposits = deposits,
                                    onNavigateToDeposit = { viewModel.setTab(ScreenTab.DEPOSIT) },
                                    onNavigateToWithdraw = { viewModel.setTab(ScreenTab.WITHDRAW) },
                                    onNavigateToAnnouncements = { viewModel.setTab(ScreenTab.ANNOUNCEMENTS) },
                                    onNavigateToProfile = { viewModel.setTab(ScreenTab.PROFILE) },
                                    onToggleSound = { viewModel.toggleSoundSetting(!(currentUser?.isSoundEnabled ?: true)) },
                                    onSwitchRoleAdmin = { viewModel.switchRoleToAdmin() }
                                )
                            }
                            ScreenTab.DEPOSIT -> {
                                SetorGmailScreen(
                                    config = appConfig,
                                    onSingleSubmit = { email, pass, rec, yr, note ->
                                        viewModel.submitSingleDeposit(email, pass, rec, yr, note)
                                    },
                                    onBulkSubmit = { list ->
                                        viewModel.submitBulkDeposits(list)
                                    }
                                )
                            }
                            ScreenTab.WITHDRAW -> {
                                WithdrawalScreen(
                                    user = currentUser,
                                    config = appConfig,
                                    withdrawals = withdrawals,
                                    onRequestWithdrawal = { method, accNum, accName, amount ->
                                        viewModel.requestWithdrawal(method, accNum, accName, amount)
                                    }
                                )
                            }
                            ScreenTab.ANNOUNCEMENTS -> {
                                AnnouncementsScreen(
                                    announcements = announcements
                                )
                            }
                            ScreenTab.PROFILE -> {
                                ProfileScreen(
                                    user = currentUser,
                                    onToggleSound = { viewModel.toggleSoundSetting(it) },
                                    onToggleVibration = { viewModel.toggleVibrationSetting(it) },
                                    onSwitchToAdmin = { viewModel.switchRoleToAdmin() },
                                    onLogout = { viewModel.logout() }
                                )
                            }
                            ScreenTab.ADMIN -> {
                                AdminDashboardScreen(
                                    config = appConfig,
                                    allDeposits = allDeposits,
                                    allWithdrawals = allWithdrawals,
                                    allUsers = allUsers,
                                    announcements = announcements,
                                    onApproveDeposit = { viewModel.approveDeposit(it) },
                                    onRejectDeposit = { dep, reason -> viewModel.rejectDeposit(dep, reason) },
                                    onCompleteWithdrawal = { viewModel.completeWithdrawal(it) },
                                    onRejectWithdrawal = { with, reason -> viewModel.rejectWithdrawal(with, reason) },
                                    onToggleMaintenance = { viewModel.toggleMaintenanceMode(it) },
                                    onUpdateRate = { viewModel.updateRate(it) },
                                    onCreateAnnouncement = { t, c, cat, imp -> viewModel.createAnnouncement(t, c, cat, imp) },
                                    onDeleteAnnouncement = { viewModel.deleteAnnouncement(it) },
                                    onSwitchRoleUser = { viewModel.switchRoleToUser() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigation(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    isAdmin: Boolean,
    pendingDepositCount: Int,
    modifier: Modifier = Modifier
) {
    val items = buildList {
        add(NavigationItem(ScreenTab.HOME, "Beranda", Icons.Filled.Home, Icons.Outlined.Home))
        add(NavigationItem(ScreenTab.DEPOSIT, "Setor Akun", Icons.Filled.AddCard, Icons.Outlined.AddCard))
        add(NavigationItem(ScreenTab.WITHDRAW, "Tarik", Icons.Filled.Payments, Icons.Outlined.Payments))
        if (isAdmin) {
            add(NavigationItem(ScreenTab.ADMIN, "Admin", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings, badgeCount = pendingDepositCount))
        } else {
            add(NavigationItem(ScreenTab.ANNOUNCEMENTS, "Info", Icons.Filled.Campaign, Icons.Outlined.Campaign))
        }
        add(NavigationItem(ScreenTab.PROFILE, "Profil", Icons.Filled.Person, Icons.Outlined.Person))
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        items.forEach { item ->
            val selected = currentTab == item.tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    if (item.badgeCount != null && item.badgeCount > 0) {
                        BadgedBox(badge = { Badge { Text(item.badgeCount.toString()) } }) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PrimaryBlue.copy(alpha = 0.15f),
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue
                ),
                modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
            )
        }
    }
}

private data class NavigationItem(
    val tab: ScreenTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null
)
