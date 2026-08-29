package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AnnouncementEntity
import com.example.data.AppConfigEntity
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.GmailDepositEntity
import com.example.data.UserEntity
import com.example.data.WithdrawalEntity
import com.example.util.SoundEffects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    HOME,
    DEPOSIT,
    WITHDRAW,
    ANNOUNCEMENTS,
    PROFILE,
    ADMIN
}

enum class AuthState {
    LOGGED_IN,
    LOGIN_SCREEN,
    REGISTER_SCREEN,
    FORGOT_PASSWORD_SCREEN
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(db)
    }

    // Auth & Navigation State
    private val _currentUserId = MutableStateFlow<Long?>(2L) // Default to demo user budi_santoso
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    private val _authState = MutableStateFlow(AuthState.LOGGED_IN)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _activeTab = MutableStateFlow(ScreenTab.HOME)
    val activeTab: StateFlow<ScreenTab> = _activeTab.asStateFlow()

    // UI Message & Toast Alert
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Flow Data
    val currentUser: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appConfig: StateFlow<AppConfigEntity?> = repository.getConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userDeposits: StateFlow<List<GmailDepositEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getDepositsForUser(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawals: StateFlow<List<WithdrawalEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getWithdrawalsForUser(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.getAllAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin flows
    val allDeposits: StateFlow<List<GmailDepositEntity>> = repository.getAllDeposits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.getAllWithdrawals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingDepositCount: StateFlow<Int> = repository.getPendingDepositsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun setTab(tab: ScreenTab) {
        _activeTab.value = tab
    }

    fun setAuthState(state: AuthState) {
        _authState.value = state
    }

    // --- Authentication Actions ---
    fun loginWithGoogle(email: String, displayName: String) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            val isAdminEmail = cleanEmail.startsWith("admin") || cleanEmail == "j14ngyanghong@gmail.com"
            val existing = repository.getUserByEmailOrPhone(cleanEmail)
            if (existing != null) {
                // Ensure admin role if recognized admin email
                if (isAdminEmail && existing.role != "ADMIN") {
                    val updated = existing.copy(role = "ADMIN")
                    repository.updateUser(updated)
                }
                _currentUserId.value = existing.id
                _authState.value = AuthState.LOGGED_IN
                _activeTab.value = if (existing.role == "ADMIN" || isAdminEmail) ScreenTab.ADMIN else ScreenTab.HOME
                _snackbarMessage.value = "Berhasil masuk via Google (${existing.email})!"
                playSound(SoundEffects.SoundType.SUCCESS_COIN)
            } else {
                // Auto register user with Google
                val name = displayName.ifBlank { cleanEmail.substringBefore("@").replace(".", " ").capitalize() }
                val newUser = UserEntity(
                    username = name,
                    email = cleanEmail,
                    phone = "",
                    passwordHash = "GOOGLE_AUTH",
                    balance = 500L, // Welcome bonus for Google user
                    referralCode = "GGL${(1000..9999).random()}",
                    role = if (isAdminEmail) "ADMIN" else "USER"
                )
                val newId = repository.registerUser(newUser)
                _currentUserId.value = newId
                _authState.value = AuthState.LOGGED_IN
                _activeTab.value = if (newUser.role == "ADMIN") ScreenTab.ADMIN else ScreenTab.HOME
                _snackbarMessage.value = "Selamat datang $name! Akun Google terhubung (+ Bonus Rp 500)!"
                playSound(SoundEffects.SoundType.SUCCESS_COIN)
            }
        }
    }

    fun login(emailOrPhone: String, password: String): Boolean {
        var success = false
        viewModelScope.launch {
            val user = repository.getUserByEmailOrPhone(emailOrPhone.trim())
            if (user != null && user.passwordHash == password.trim()) {
                _currentUserId.value = user.id
                _authState.value = AuthState.LOGGED_IN
                _activeTab.value = if (user.role == "ADMIN") ScreenTab.ADMIN else ScreenTab.HOME
                _snackbarMessage.value = "Selamat datang kembali, ${user.username}!"
                playSound(SoundEffects.SoundType.SUCCESS_COIN)
                success = true
            } else {
                _snackbarMessage.value = "Email / Nomor WhatsApp atau Password salah!"
                playSound(SoundEffects.SoundType.ALERT_WARNING)
            }
        }
        return success
    }

    fun quickLogin(asAdmin: Boolean) {
        viewModelScope.launch {
            if (asAdmin) {
                _currentUserId.value = 1L
                _authState.value = AuthState.LOGGED_IN
                _activeTab.value = ScreenTab.ADMIN
                _snackbarMessage.value = "Masuk sebagai Administrator (admin_master)"
            } else {
                _currentUserId.value = 2L
                _authState.value = AuthState.LOGGED_IN
                _activeTab.value = ScreenTab.HOME
                _snackbarMessage.value = "Masuk sebagai Pengguna (budi_santoso)"
            }
            playSound(SoundEffects.SoundType.NOTIFICATION_PING)
        }
    }

    fun register(
        username: String,
        email: String,
        phone: String,
        password: String,
        referralCode: String
    ) {
        viewModelScope.launch {
            val existing = repository.getUserByEmailOrPhone(email.trim()) ?: repository.getUserByEmailOrPhone(phone.trim())
            if (existing != null) {
                _snackbarMessage.value = "Email atau Nomor Telepon sudah terdaftar!"
                playSound(SoundEffects.SoundType.ALERT_WARNING)
                return@launch
            }

            val newUser = UserEntity(
                username = username.trim().ifBlank { "Mitra_${System.currentTimeMillis() % 10000}" },
                email = email.trim(),
                phone = phone.trim(),
                passwordHash = password.trim(),
                balance = if (referralCode.isNotBlank()) 500L else 0L, // Welcome bonus if referral used
                referralCode = "REF${(1000..9999).random()}",
                role = "USER"
            )
            val newId = repository.registerUser(newUser)
            _currentUserId.value = newId
            _authState.value = AuthState.LOGGED_IN
            _activeTab.value = ScreenTab.HOME
            val bonusText = if (referralCode.isNotBlank()) " + Bonus Referral Rp 500!" else ""
            _snackbarMessage.value = "Pendaftaran Berhasil! Selamat datang di Setor Gmail Rewards$bonusText"
            playSound(SoundEffects.SoundType.SUCCESS_COIN)
        }
    }

    fun resetPassword(emailOrPhone: String, newPass: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmailOrPhone(emailOrPhone.trim())
            if (user != null) {
                repository.updateUser(user.copy(passwordHash = newPass.trim()))
                _snackbarMessage.value = "Password berhasil direset! Silakan login dengan password baru."
                _authState.value = AuthState.LOGIN_SCREEN
                playSound(SoundEffects.SoundType.SUCCESS_COIN)
            } else {
                _snackbarMessage.value = "Akun dengan email/nomor tersebut tidak ditemukan."
                playSound(SoundEffects.SoundType.ALERT_WARNING)
            }
        }
    }

    fun logout() {
        _currentUserId.value = null
        _authState.value = AuthState.LOGIN_SCREEN
        _activeTab.value = ScreenTab.HOME
        _snackbarMessage.value = "Anda telah keluar dari akun."
    }

    fun switchRoleToAdmin() {
        viewModelScope.launch {
            _currentUserId.value = 1L
            _activeTab.value = ScreenTab.ADMIN
            _snackbarMessage.value = "Beralih ke Panel Administrator"
            playSound(SoundEffects.SoundType.NOTIFICATION_PING)
        }
    }

    fun switchRoleToUser() {
        viewModelScope.launch {
            _currentUserId.value = 2L
            _activeTab.value = ScreenTab.HOME
            _snackbarMessage.value = "Beralih ke Mode Pengguna"
            playSound(SoundEffects.SoundType.NOTIFICATION_PING)
        }
    }

    // --- User Submissions ---
    fun submitSingleDeposit(
        email: String,
        password: String,
        recovery: String,
        year: String,
        note: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val currentRate = appConfig.value?.currentRatePerAccount ?: 2500L
            val deposit = GmailDepositEntity(
                userId = user.id,
                username = user.username,
                email = email.trim(),
                password = password.trim(),
                recoveryInfo = recovery.trim(),
                accountYear = year,
                status = "PENDING",
                rewardAmount = currentRate,
                note = note.trim()
            )
            repository.submitDeposit(deposit)
            _snackbarMessage.value = "Akun Gmail $email berhasil disetor! Menunggu verifikasi tim."
            playSound(SoundEffects.SoundType.DEPOSIT_SUBMIT)
            _activeTab.value = ScreenTab.HOME
        }
    }

    fun submitBulkDeposits(
        parsedList: List<GmailDepositEntity>
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val userSpecificList = parsedList.map { it.copy(userId = user.id, username = user.username) }
            repository.submitBulkDeposits(userSpecificList)
            _snackbarMessage.value = "${userSpecificList.size} Akun Gmail berhasil disetor sekaligus!"
            playSound(SoundEffects.SoundType.DEPOSIT_SUBMIT)
            _activeTab.value = ScreenTab.HOME
        }
    }

    // --- Withdrawal Actions ---
    fun requestWithdrawal(
        method: String,
        accountNumber: String,
        accountName: String,
        amount: Long
    ): Boolean {
        val user = currentUser.value ?: return false
        val minAmount = appConfig.value?.minWithdrawalAmount ?: 25000L

        if (amount < minAmount) {
            _snackbarMessage.value = "Minimal penarikan saldo adalah Rp $minAmount"
            playSound(SoundEffects.SoundType.ALERT_WARNING)
            return false
        }
        if (amount > user.balance) {
            _snackbarMessage.value = "Saldo tidak mencukupi untuk melakukan penarikan ini."
            playSound(SoundEffects.SoundType.ALERT_WARNING)
            return false
        }

        viewModelScope.launch {
            val withdrawal = WithdrawalEntity(
                userId = user.id,
                username = user.username,
                method = method,
                accountNumber = accountNumber.trim(),
                accountHolderName = accountName.trim(),
                amount = amount,
                status = "PROCESSED",
                adminNote = "Permintaan penarikan sedang diproses sistem otomatis"
            )
            repository.requestWithdrawal(withdrawal)
            _snackbarMessage.value = "Permintaan penarikan sebesar Rp $amount ke $method ($accountNumber) berhasil diajukan!"
            playSound(SoundEffects.SoundType.WITHDRAWAL_REQUEST)
        }
        return true
    }

    // --- Admin Actions ---
    fun approveDeposit(deposit: GmailDepositEntity) {
        viewModelScope.launch {
            repository.approveDeposit(deposit.id, deposit.rewardAmount, deposit.userId)
            _snackbarMessage.value = "Setoran ${deposit.email} DISETUJUI! Reward Rp ${deposit.rewardAmount} ditambahkan ke saldo user."
            playSound(SoundEffects.SoundType.SUCCESS_COIN)
        }
    }

    fun rejectDeposit(deposit: GmailDepositEntity, reason: String) {
        viewModelScope.launch {
            val actualReason = reason.ifBlank { "Password tidak cocok / 2FA aktif" }
            repository.rejectDeposit(deposit.id, actualReason)
            _snackbarMessage.value = "Setoran ${deposit.email} DITOLAK ($actualReason)"
            playSound(SoundEffects.SoundType.ALERT_WARNING)
        }
    }

    fun completeWithdrawal(withdrawal: WithdrawalEntity, note: String = "Transfer Berhasil") {
        viewModelScope.launch {
            repository.updateWithdrawalStatus(withdrawal.id, "SUCCESS", note)
            _snackbarMessage.value = "Penarikan Rp ${withdrawal.amount} ke ${withdrawal.method} dinyatakan SUKSES."
            playSound(SoundEffects.SoundType.SUCCESS_COIN)
        }
    }

    fun rejectWithdrawal(withdrawal: WithdrawalEntity, reason: String) {
        viewModelScope.launch {
            repository.updateWithdrawalStatus(withdrawal.id, "REJECTED", reason)
            // Refund balance to user
            val targetUser = repository.getUserById(withdrawal.userId)
            if (targetUser != null) {
                repository.updateBalance(withdrawal.userId, targetUser.balance + withdrawal.amount)
            }
            _snackbarMessage.value = "Penarikan DITOLAK. Saldo Rp ${withdrawal.amount} dikembalikan ke user."
            playSound(SoundEffects.SoundType.ALERT_WARNING)
        }
    }

    fun toggleMaintenanceMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setMaintenanceMode(enabled)
            _snackbarMessage.value = if (enabled) "Mode Maintenance Server DIAKTIFKAN" else "Mode Maintenance Server DINONAKTIFKAN (Normal)"
            playSound(SoundEffects.SoundType.NOTIFICATION_PING)
        }
    }

    fun updateRate(rate: Long) {
        viewModelScope.launch {
            repository.setRate(rate)
            _snackbarMessage.value = "Rate reward per akun diperbarui menjadi Rp $rate"
            playSound(SoundEffects.SoundType.SUCCESS_COIN)
        }
    }

    fun createAnnouncement(title: String, content: String, category: String, isImportant: Boolean) {
        viewModelScope.launch {
            val announcement = AnnouncementEntity(
                title = title.trim(),
                content = content.trim(),
                category = category,
                isImportant = isImportant,
                author = "Admin Official",
                dateFormatted = "Hari Ini, ${com.example.util.Formatters.formatDate(System.currentTimeMillis())}"
            )
            repository.createAnnouncement(announcement)
            _snackbarMessage.value = "Pengumuman publik berhasil dipublikasikan!"
            playSound(SoundEffects.SoundType.NOTIFICATION_PING)
        }
    }

    fun deleteAnnouncement(id: Long) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id)
            _snackbarMessage.value = "Pengumuman berhasil dihapus."
        }
    }

    fun toggleSoundSetting(enabled: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateSoundSetting(user.id, enabled)
            _snackbarMessage.value = if (enabled) "Suara notifikasi diaktifkan" else "Suara notifikasi dinonaktifkan"
            if (enabled) {
                SoundEffects.playSound(getApplication(), SoundEffects.SoundType.NOTIFICATION_PING, true)
            }
        }
    }

    fun toggleVibrationSetting(enabled: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateVibrationSetting(user.id, enabled)
            _snackbarMessage.value = if (enabled) "Getar notifikasi diaktifkan" else "Getar notifikasi dinonaktifkan"
            if (enabled) {
                SoundEffects.vibrate(getApplication(), true)
            }
        }
    }

    fun playSound(type: SoundEffects.SoundType) {
        val isSoundOn = currentUser.value?.isSoundEnabled ?: true
        val isVibOn = currentUser.value?.isVibrationEnabled ?: true
        SoundEffects.playSound(getApplication(), type, isSoundOn)
        SoundEffects.vibrate(getApplication(), isVibOn)
    }
}
