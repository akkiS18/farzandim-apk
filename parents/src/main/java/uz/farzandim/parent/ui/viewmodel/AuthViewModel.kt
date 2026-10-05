package uz.farzandim.parent.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.farzandim.parent.data.local.SessionManager
import uz.farzandim.parent.data.model.SchoolDto
import uz.farzandim.parent.data.repository.AuthRepository

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Authenticated : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _availableSchools = MutableStateFlow<List<SchoolDto>>(emptyList())
    val availableSchools: StateFlow<List<SchoolDto>> = _availableSchools.asStateFlow()

    private val _isLoadingSchools = MutableStateFlow(false)
    val isLoadingSchools: StateFlow<Boolean> = _isLoadingSchools.asStateFlow()

    init {
        loadSchools()
    }

    fun loadSchools() {
        viewModelScope.launch {
            _isLoadingSchools.value = true
            val result = repository.getSchools()
            result.onSuccess { list ->
                _availableSchools.value = list
            }
            _isLoadingSchools.value = false
        }
    }

    fun selectSchool(school: SchoolDto) {
        viewModelScope.launch {
            repository.selectSchool(school)
        }
    }

    val subdomain: StateFlow<String> = sessionManager.subdomainFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionManager.DEFAULT_SUBDOMAIN)

    val schoolName: StateFlow<String?> = sessionManager.schoolNameFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isPinSet: StateFlow<Boolean?> = sessionManager.isPinSetFlow
        .map<Boolean, Boolean?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val biometricEnabled: StateFlow<Boolean> = sessionManager.biometricEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val userFullName: StateFlow<String> = sessionManager.userFullNameFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val language: StateFlow<String> = sessionManager.languageFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "uz")

    val isAuthenticated: StateFlow<Boolean?> = sessionManager.tokenFlow
        .map<String?, Boolean?> { !it.isNullOrBlank() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun loginWithPassport(passport: String, password: String) {
        if (passport.isBlank()) {
            _uiState.value = AuthUiState.Error("Pasport seriyasini kiriting")
            return
        }
        if (password.isBlank()) {
            _uiState.value = AuthUiState.Error("Parolni kiriting")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.loginWithPassport(passport, password)
                .onSuccess {
                    _uiState.value = AuthUiState.Authenticated
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(it.message ?: "Kirishda xatolik yuz berdi")
                }
        }
    }

    fun saveSubdomain(subdomain: String) {
        viewModelScope.launch {
            repository.saveSubdomain(subdomain)
        }
    }

    fun saveBaseUrl(url: String) {
        viewModelScope.launch {
            repository.saveBaseUrl(url)
        }
    }

    fun setPin(pin: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.setPin(pin)
            onComplete()
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        return repository.verifyPin(pin)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBiometricEnabled(enabled)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            repository.setLanguage(lang)
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.logout()
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Logout error in repository", e)
            } finally {
                _uiState.value = AuthUiState.Idle
                onComplete()
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
