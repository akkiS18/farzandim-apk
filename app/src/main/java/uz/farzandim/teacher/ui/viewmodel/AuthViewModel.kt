package uz.farzandim.teacher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.farzandim.teacher.data.local.SessionManager
import uz.farzandim.teacher.data.local.entity.SchoolEntity
import uz.farzandim.teacher.data.repository.AuthRepository

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Authenticated : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    val schools: StateFlow<List<SchoolEntity>> = authRepository.getSchoolsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedSchool: StateFlow<SchoolEntity?> = authRepository.getSelectedSchoolFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userFullName: StateFlow<String> = authRepository.userFullNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "O'qituvchi")

    val subdomain: StateFlow<String> = authRepository.subdomainFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "test_school")

    val currentSchoolName: StateFlow<String> = combine(
        selectedSchool,
        sessionManager.schoolNameFlow,
        subdomain
    ) { selSchool, savedName, sub ->
        when {
            !selSchool?.name.isNullOrBlank() && selSchool!!.name != "Farzandim" && selSchool.name != "Maktab" -> {
                selSchool.name
            }
            !savedName.isNullOrBlank() && savedName != "Farzandim" && savedName != "Maktab" -> {
                savedName
            }
            else -> {
                SessionManager.formatSubdomainToSchoolName(sub)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionManager.formatSubdomainToSchoolName(SessionManager.DEFAULT_SUBDOMAIN))

    val baseUrl: StateFlow<String> = authRepository.baseUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionManager.DEFAULT_BASE_URL)

    private val _availableSchools = MutableStateFlow<List<uz.farzandim.teacher.data.model.SchoolDto>>(emptyList())
    val availableSchools: StateFlow<List<uz.farzandim.teacher.data.model.SchoolDto>> = _availableSchools.asStateFlow()

    private val _isLoadingSchools = MutableStateFlow(false)
    val isLoadingSchools: StateFlow<Boolean> = _isLoadingSchools.asStateFlow()

    fun loadSchools() {
        viewModelScope.launch {
            _isLoadingSchools.value = true
            val result = authRepository.fetchSchools()
            result.onSuccess { list ->
                _availableSchools.value = list
            }
            _isLoadingSchools.value = false
        }
    }

    fun selectSchool(school: uz.farzandim.teacher.data.model.SchoolDto) {
        viewModelScope.launch {
            authRepository.selectSchool(school.id, school.name, school.subdomain)
        }
    }

    fun saveSubdomain(subdomain: String) {
        viewModelScope.launch {
            authRepository.saveSubdomain(subdomain)
        }
    }

    fun setBaseUrl(url: String) {
        viewModelScope.launch {
            authRepository.setBaseUrl(url)
        }
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isSessionChecked = MutableStateFlow(false)
    val isSessionChecked: StateFlow<Boolean> = _isSessionChecked.asStateFlow()

    init {
        checkSession()
        loadSchools()
    }

    fun checkSession() {
        viewModelScope.launch {
            if (authRepository.isOfflineSessionValid()) {
                _uiState.value = AuthUiState.Authenticated
            }
            _isSessionChecked.value = true
        }
    }

    fun login(phoneInput: String, password: String) {
        val digits = phoneInput.filter { it.isDigit() }
        val cleanPhone = if (phoneInput.startsWith("+")) "+$digits" else if (digits.startsWith("998")) "+$digits" else "+998$digits"
        if (digits.isBlank()) {
            _uiState.value = AuthUiState.Error("Iltimos, telefon raqamingizni kiriting")
            return
        }
        if (digits.length < 9) {
            _uiState.value = AuthUiState.Error("Iltimos, to'liq telefon raqamingizni kiriting")
            return
        }
        if (password.isBlank()) {
            _uiState.value = AuthUiState.Error("Iltimos, parolni kiriting")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(cleanPhone, password)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Authenticated
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Tizimga kirishda xatolik yuz berdi")
                }
            )
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Logout error in repository", e)
            } finally {
                _uiState.value = AuthUiState.Idle
                onLoggedOut()
            }
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
