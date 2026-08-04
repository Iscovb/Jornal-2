package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.repository.TradingRepository
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.AddEditTradeScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.PlaybooksScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.JournnexTheme
import com.example.ui.theme.TradingBackground
import com.example.ui.theme.TradingCardBorder
import com.example.ui.theme.TradingPrimary
import com.example.ui.theme.TradingSurface
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingTextPrimary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Analytics", Icons.Default.Analytics, "tab_dashboard"),
    JOURNAL("Journal", Icons.Default.MenuBook, "tab_journal"),
    CALENDAR("P&L Grid", Icons.Default.CalendarMonth, "tab_calendar"),
    PLAYBOOKS("Playbooks", Icons.Default.AutoAwesome, "tab_playbooks"),
    ACCOUNTS("Accounts", Icons.Default.AccountBalanceWallet, "tab_accounts"),
    PROFILE("Profile", Icons.Default.Person, "tab_profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JournnexTheme {
                val context = LocalContext.current
                val database = remember { AppDatabase.getDatabase(context) }
                val repository = remember {
                    TradingRepository(
                        accountDao = database.accountDao(),
                        tradeDao = database.tradeDao(),
                        playbookDao = database.playbookDao(),
                        complianceDao = database.complianceDao()
                    )
                }

                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(repository)
                )

                JournnexApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun JournnexApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var isAddingTrade by remember { mutableStateOf(false) }
    var showAuthScreen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userNotification) {
        uiState.userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    LaunchedEffect(uiState.userProfile) {
        if (uiState.userProfile != null) {
            showAuthScreen = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = TradingBackground,
        bottomBar = {
            if (!isAddingTrade && !showAuthScreen) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .border(1.dp, TradingCardBorder)
                        .testTag("bottom_navigation_bar"),
                    containerColor = TradingSurface
                ) {
                    NavigationTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) TradingPrimary else TradingTextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TradingPrimary else TradingTextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = TradingPrimary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TradingBackground)
        ) {
            if (showAuthScreen) {
                AuthScreen(
                    isLoading = uiState.isAuthLoading,
                    errorMessage = uiState.authError,
                    onSignUp = { email, pass, name -> viewModel.signUpWithEmail(email, pass, name) },
                    onSignIn = { email, pass -> viewModel.signInWithEmail(email, pass) },
                    onGoogleSignIn = { viewModel.signInWithGoogleCredential("google_id_token_demo") },
                    onDemoLogin = { viewModel.signInDemoUser(); showAuthScreen = false }
                )
            } else if (isAddingTrade) {
                AddEditTradeScreen(
                    uiState = uiState,
                    onSaveTrade = { trade, ruleIds ->
                        viewModel.saveTrade(trade, ruleIds)
                        isAddingTrade = false
                    },
                    onCancel = { isAddingTrade = false }
                )
            } else {
                when (currentTab) {
                    NavigationTab.DASHBOARD -> DashboardScreen(uiState = uiState)
                    NavigationTab.JOURNAL -> JournalScreen(
                        uiState = uiState,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onSessionFilterChange = { viewModel.setSessionFilter(it) },
                        onSetupFilterChange = { viewModel.setSetupFilter(it) },
                        onStatusFilterChange = { viewModel.setStatusFilter(it) },
                        onDeleteTrade = { viewModel.deleteTrade(it) },
                        onAddTradeClick = { isAddingTrade = true }
                    )
                    NavigationTab.CALENDAR -> CalendarScreen(
                        uiState = uiState,
                        onPrevMonth = { viewModel.prevCalendarMonth() },
                        onNextMonth = { viewModel.nextCalendarMonth() }
                    )
                    NavigationTab.PLAYBOOKS -> PlaybooksScreen(
                        uiState = uiState,
                        onCreatePlaybook = { title, desc, tf, wg, tr, rules ->
                            viewModel.createPlaybook(title, desc, tf, wg, tr, rules)
                        },
                        onDeletePlaybook = { viewModel.deletePlaybook(it) }
                    )
                    NavigationTab.ACCOUNTS -> AccountsScreen(
                        uiState = uiState,
                        onCreateAccount = { name, broker, bal, curr ->
                            viewModel.createAccount(name, broker, bal, curr)
                        },
                        onSelectAccount = { viewModel.selectAccount(it) },
                        onImportCsv = { csv, accId ->
                            viewModel.importCsv(csv, accId)
                        },
                        onCurrencyChange = { viewModel.setPreferredCurrency(it) }
                    )
                    NavigationTab.PROFILE -> ProfileScreen(
                        uiState = uiState,
                        onSaveProfile = { viewModel.saveUserProfile(it) },
                        onSignOut = { viewModel.signOut() },
                        onOpenAuth = { showAuthScreen = true }
                    )
                }
            }
        }
    }
}
