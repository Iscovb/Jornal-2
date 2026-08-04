package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BreakdownItem
import com.example.data.model.DailyPnL
import com.example.data.model.EquityPoint
import com.example.data.model.PerformanceMetrics
import com.example.data.model.Playbook
import com.example.data.model.StrategyRule
import com.example.data.model.Trade
import com.example.data.model.TradeRuleCompliance
import com.example.data.model.TradingAccount
import com.example.data.model.UserProfile
import com.example.data.parser.CsvImportParser
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class TradingUiState(
    val accounts: List<TradingAccount> = emptyList(),
    val selectedAccountId: Long? = null, // null = All Accounts
    val selectedAccountName: String = "All Accounts",
    val preferredCurrency: String = "USD",
    val trades: List<Trade> = emptyList(),
    val filteredTrades: List<Trade> = emptyList(),
    val metrics: PerformanceMetrics = PerformanceMetrics(),
    val equityCurve: List<EquityPoint> = emptyList(),
    val breakdownByDay: List<BreakdownItem> = emptyList(),
    val breakdownBySession: List<BreakdownItem> = emptyList(),
    val breakdownBySetup: List<BreakdownItem> = emptyList(),
    val breakdownByAsset: List<BreakdownItem> = emptyList(),
    val calendarYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val calendarMonth: Int = Calendar.getInstance().get(Calendar.MONTH), // 0-indexed
    val monthlyCalendar: List<DailyPnL> = emptyList(),
    val playbooks: List<Playbook> = emptyList(),
    val rulesMap: Map<Long, List<StrategyRule>> = emptyMap(),
    val searchQuery: String = "",
    val sessionFilter: String = "ALL",
    val setupFilter: String = "ALL",
    val statusFilter: String = "ALL",
    val isLoading: Boolean = false,
    val userNotification: String? = null,
    val userProfile: UserProfile? = null,
    val isAuthLoading: Boolean = false,
    val authError: String? = null
)

class MainViewModel(private val repository: TradingRepository) : ViewModel() {

    val authRepository = FirebaseAuthRepository()

    private val _selectedAccountId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _sessionFilter = MutableStateFlow("ALL")
    private val _setupFilter = MutableStateFlow("ALL")
    private val _statusFilter = MutableStateFlow("ALL")

    private val calendar = Calendar.getInstance()
    private val _calendarYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    private val _calendarMonth = MutableStateFlow(calendar.get(Calendar.MONTH))

    private val _preferredCurrency = MutableStateFlow("USD")
    private val _userNotification = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TradingUiState> = combine(
        repository.accounts,
        repository.allTrades,
        repository.playbooks,
        repository.allRules,
        repository.allCompliance,
        _selectedAccountId,
        _searchQuery,
        _sessionFilter,
        _setupFilter,
        _statusFilter,
        _calendarYear,
        _calendarMonth,
        authRepository.userProfile,
        authRepository.isLoading,
        authRepository.authError
    ) { args ->
        val accounts = args[0] as List<TradingAccount>
        val allTrades = args[1] as List<Trade>
        val playbooks = args[2] as List<Playbook>
        val allRules = args[3] as List<StrategyRule>
        val allCompliance = args[4] as List<TradeRuleCompliance>
        val selectedAccountId = args[5] as Long?
        val query = args[6] as String
        val sessionF = args[7] as String
        val setupF = args[8] as String
        val statusF = args[9] as String
        val year = args[10] as Int
        val month = args[11] as Int
        val userProfile = args[12] as UserProfile?
        val isAuthLoading = args[13] as Boolean
        val authError = args[14] as String?

        // 1. Filter trades by account
        val accountTrades = if (selectedAccountId == null || selectedAccountId == 0L) {
            allTrades
        } else {
            allTrades.filter { it.accountId == selectedAccountId }
        }

        // 2. Filter trades by search query & tags
        val filtered = accountTrades.filter { trade ->
            val matchesQuery = query.isBlank() || 
                    trade.symbol.contains(query, ignoreCase = true) ||
                    trade.setupTag.contains(query, ignoreCase = true) ||
                    trade.executionNotes.contains(query, ignoreCase = true)

            val matchesSession = sessionF == "ALL" || trade.session.equals(sessionF, ignoreCase = true)
            val matchesSetup = setupF == "ALL" || trade.setupTag.equals(setupF, ignoreCase = true)
            val matchesStatus = statusF == "ALL" || trade.status.equals(statusF, ignoreCase = true)

            matchesQuery && matchesSession && matchesSetup && matchesStatus
        }

        // 3. Performance Metrics
        val metrics = repository.calculateMetrics(accountTrades, allCompliance)

        // 4. Equity Curve
        val equityPoints = repository.calculateEquityCurve(accountTrades)

        // 5. Breakdowns
        val dayBreakdown = repository.calculateBreakdownByDay(accountTrades)
        val sessionBreakdown = repository.calculateBreakdownBySession(accountTrades)
        val setupBreakdown = repository.calculateBreakdownBySetup(accountTrades)
        val assetBreakdown = repository.calculateBreakdownByAsset(accountTrades)

        // 6. Calendar
        val calendarData = repository.calculateMonthlyCalendar(accountTrades, year, month)

        // 7. Rules map by playbook ID
        val rulesMap = allRules.groupBy { it.playbookId }

        val selectedAccName = if (selectedAccountId == null || selectedAccountId == 0L) {
            "All Accounts Combined"
        } else {
            accounts.find { it.id == selectedAccountId }?.name ?: "Account #$selectedAccountId"
        }

        TradingUiState(
            accounts = accounts,
            selectedAccountId = selectedAccountId,
            selectedAccountName = selectedAccName,
            preferredCurrency = _preferredCurrency.value,
            trades = accountTrades,
            filteredTrades = filtered,
            metrics = metrics,
            equityCurve = equityPoints,
            breakdownByDay = dayBreakdown,
            breakdownBySession = sessionBreakdown,
            breakdownBySetup = setupBreakdown,
            breakdownByAsset = assetBreakdown,
            calendarYear = year,
            calendarMonth = month,
            monthlyCalendar = calendarData,
            playbooks = playbooks,
            rulesMap = rulesMap,
            searchQuery = query,
            sessionFilter = sessionF,
            setupFilter = setupF,
            statusFilter = statusF,
            userNotification = _userNotification.value,
            userProfile = userProfile,
            isAuthLoading = isAuthLoading,
            authError = authError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TradingUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty()
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String) {
        viewModelScope.launch {
            val success = authRepository.signUpWithEmail(email, pass, name)
            if (success) {
                showNotification("Account created & saved to Firestore!")
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            val success = authRepository.signInWithEmail(email, pass)
            if (success) {
                showNotification("Logged in! Data synchronized with Firestore.")
            }
        }
    }

    fun signInWithGoogleCredential(idToken: String) {
        viewModelScope.launch {
            val success = authRepository.signInWithGoogleCredential(idToken)
            if (success) {
                showNotification("Signed in with Google!")
            }
        }
    }

    fun signInDemoUser() {
        authRepository.signInDemoUser()
        showNotification("Signed in to Demo Firebase Mode!")
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            authRepository.saveUserProfileToFirestore(profile) { success ->
                if (success) {
                    showNotification("Profile & Risk Targets updated in Firestore!")
                } else {
                    showNotification("Failed to update Firestore profile.")
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        showNotification("Signed out")
    }

    fun clearAuthError() {
        authRepository.clearError()
    }

    fun selectAccount(accountId: Long?) {
        _selectedAccountId.value = accountId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSessionFilter(session: String) {
        _sessionFilter.value = session
    }

    fun setSetupFilter(setup: String) {
        _setupFilter.value = setup
    }

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun setCalendarMonth(year: Int, month: Int) {
        _calendarYear.value = year
        _calendarMonth.value = month
    }

    fun prevCalendarMonth() {
        var m = _calendarMonth.value - 1
        var y = _calendarYear.value
        if (m < 0) {
            m = 11
            y -= 1
        }
        _calendarMonth.value = m
        _calendarYear.value = y
    }

    fun nextCalendarMonth() {
        var m = _calendarMonth.value + 1
        var y = _calendarYear.value
        if (m > 11) {
            m = 0
            y += 1
        }
        _calendarMonth.value = m
        _calendarYear.value = y
    }

    fun setPreferredCurrency(currency: String) {
        _preferredCurrency.value = currency
    }

    fun saveTrade(trade: Trade, followedRuleIds: List<Long> = emptyList()) {
        viewModelScope.launch {
            val id = repository.saveTrade(trade)
            if (followedRuleIds.isNotEmpty()) {
                val compliance = followedRuleIds.map { ruleId ->
                    TradeRuleCompliance(tradeId = id, ruleId = ruleId, isFollowed = true)
                }
                repository.saveTradeCompliance(compliance)
            }
            showNotification("Trade saved successfully!")
        }
    }

    fun deleteTrade(trade: Trade) {
        viewModelScope.launch {
            repository.deleteTrade(trade)
            showNotification("Trade deleted")
        }
    }

    fun createAccount(name: String, broker: String, initialBalance: Double, currency: String) {
        viewModelScope.launch {
            val account = TradingAccount(
                name = name.ifBlank { "New Account" },
                brokerName = broker.ifBlank { "Generic Broker" },
                initialBalance = if (initialBalance > 0) initialBalance else 10000.0,
                currency = currency
            )
            val newId = repository.createAccount(account)
            _selectedAccountId.value = newId
            showNotification("Account created!")
        }
    }

    fun createPlaybook(title: String, description: String, timeframe: String, winRateGoal: Double, targetRR: Double, rules: List<String>) {
        viewModelScope.launch {
            val pbId = repository.savePlaybook(
                Playbook(
                    title = title,
                    description = description,
                    timeframe = timeframe,
                    winRateGoal = winRateGoal,
                    targetRiskReward = targetRR
                )
            )
            rules.forEach { ruleText ->
                if (ruleText.isNotBlank()) {
                    repository.saveStrategyRule(
                        StrategyRule(playbookId = pbId, ruleText = ruleText, isRequired = true)
                    )
                }
            }
            showNotification("Playbook & rules added!")
        }
    }

    fun deletePlaybook(playbook: Playbook) {
        viewModelScope.launch {
            repository.deletePlaybook(playbook)
            showNotification("Playbook deleted")
        }
    }

    fun importCsv(csvText: String, targetAccountId: Long) {
        viewModelScope.launch {
            val parsed = CsvImportParser.parseCsvText(csvText, targetAccountId)
            if (parsed.isNotEmpty()) {
                repository.saveTrades(parsed)
                showNotification("Successfully imported ${parsed.size} trades from CSV!")
            } else {
                showNotification("No valid trades found in CSV snippet.")
            }
        }
    }

    fun clearNotification() {
        _userNotification.value = null
    }

    private fun showNotification(msg: String) {
        _userNotification.value = msg
    }
}

class MainViewModelFactory(private val repository: TradingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
