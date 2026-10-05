package uz.farzandim.teacher.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import uz.farzandim.teacher.data.local.AppDatabase
import uz.farzandim.teacher.data.local.SessionManager
import uz.farzandim.teacher.data.local.dao.SchoolDao
import uz.farzandim.teacher.data.local.dao.SessionDao
import uz.farzandim.teacher.data.local.entity.SchoolEntity
import uz.farzandim.teacher.data.local.entity.UserSessionEntity
import uz.farzandim.teacher.data.model.LoginRequest
import uz.farzandim.teacher.data.model.SchoolDto
import uz.farzandim.teacher.data.remote.ApiService

class AuthRepository(
    private val apiService: ApiService,
    private val schoolDao: SchoolDao,
    private val sessionDao: SessionDao,
    private val sessionManager: SessionManager,
    private val appDatabase: AppDatabase
) {

    fun getSchoolsFlow(): Flow<List<SchoolEntity>> = schoolDao.getAllSchoolsFlow()
    fun getSelectedSchoolFlow(): Flow<SchoolEntity?> = schoolDao.getSelectedSchoolFlow()
    val tokenFlow: Flow<String?> = sessionManager.tokenFlow
    val userFullNameFlow: Flow<String> = sessionManager.userFullNameFlow
    val baseUrlFlow: Flow<String> = sessionManager.baseUrlFlow
    val subdomainFlow: Flow<String> = sessionManager.subdomainFlow

    suspend fun setBaseUrl(url: String) = sessionManager.setBaseUrl(url)
    suspend fun saveSubdomain(subdomain: String) = sessionManager.saveSubdomain(subdomain)

    suspend fun fetchSchools(): Result<List<SchoolDto>> {
        return try {
            val response = apiService.getSchools()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!
                schoolDao.insertSchools(list.map { SchoolEntity(it.id, it.name) })
                Result.success(list)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Maktablarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            // If offline, fallback to cached schools
            val cached = schoolDao.getAllSchools()
            if (cached.isNotEmpty()) {
                Result.success(cached.map { SchoolDto(it.id, it.name) })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun selectSchool(schoolId: String, schoolName: String, subdomain: String? = null) {
        schoolDao.clearSelectedSchool()
        schoolDao.setSelectedSchool(schoolId)
        sessionManager.saveSelectedSchool(schoolId, schoolName)
        if (!subdomain.isNullOrBlank()) {
            sessionManager.saveSubdomain(subdomain)
        }
    }

    suspend fun login(phone: String, password: String): Result<Unit> {
        val inputClean = phone.trim()
        if (inputClean.isBlank()) {
            return Result.failure(Exception("Iltimos, telefon raqamingizni kiriting"))
        }

        // If user enters passport format or phone
        val isPassport = inputClean.matches(Regex("^[A-Za-z]{2}\\s*\\d{7}$"))
        val request = if (isPassport) {
            LoginRequest(
                documentNo = inputClean.uppercase().replace(" ", ""),
                password = password
            )
        } else {
            val digitsOnly = inputClean.replace(Regex("[^0-9+]"), "")
            val formattedPhone = if (digitsOnly.startsWith("+")) {
                digitsOnly
            } else if (digitsOnly.startsWith("998") && digitsOnly.length == 12) {
                "+$digitsOnly"
            } else if (digitsOnly.length == 9) {
                "+998$digitsOnly"
            } else {
                digitsOnly
            }
            LoginRequest(
                phone = formattedPhone,
                password = password
            )
        }

        // Do NOT pass X-School-ID on login — backend resolves tenant via Origin header.
        // After login, the server returns user.schoolId which we then store.
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val user = data.user

                if (user.role != "MAIN_TEACHER" && user.role != "SUBJECT_TEACHER" && user.role != "ADMIN") {
                    return Result.failure(Exception("Ushbu ilova faqat o'qituvchilar uchun mo'ljallangan"))
                }

                val currentSub = sessionManager.getSubdomain()
                val fallbackSchoolName = SessionManager.formatSubdomainToSchoolName(currentSub)
                val resolvedSchoolId = user.schoolId ?: ""
                val schoolName = schoolDao.getSelectedSchool()?.name?.takeIf { it.isNotBlank() && it != "Farzandim" && it != "Maktab" }
                    ?: sessionManager.getSchoolName()?.takeIf { it.isNotBlank() && it != "Farzandim" && it != "Maktab" }
                    ?: fallbackSchoolName

                // 1. Save in DataStore
                sessionManager.saveAuth(
                    token = data.token,
                    refreshToken = data.refreshToken,
                    userId = user.id,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    role = user.role,
                    schoolId = resolvedSchoolId
                )

                if (resolvedSchoolId.isNotEmpty()) {
                    sessionManager.saveSelectedSchool(resolvedSchoolId, schoolName)
                    schoolDao.insertSchools(listOf(SchoolEntity(resolvedSchoolId, schoolName, isSelected = true)))
                }

                // 2. Cache in Room for offline session verification
                sessionDao.insertSession(
                    UserSessionEntity(
                        id = user.id,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        middleName = user.middleName,
                        role = user.role,
                        phone = user.phone ?: request.phone,
                        documentNo = user.documentNo ?: request.documentNo,
                        schoolId = resolvedSchoolId,
                        schoolName = schoolName,
                        token = data.token,
                        refreshToken = data.refreshToken
                    )
                )

                Result.success(Unit)
            } else {
                val rawError = response.errorBody()?.string() ?: ""
                val errorMsg = try {
                    org.json.JSONObject(rawError).optString("error", "Telefon raqami yoki parol noto'g'ri")
                } catch (e: Exception) {
                    "Telefon raqami yoki parol noto'g'ri"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Serverga ulanib bo'lmadi"))
        }
    }

    suspend fun isOfflineSessionValid(): Boolean {
        val token = sessionManager.getToken()
        val session = sessionDao.getSession()
        return !token.isNullOrEmpty() && session != null
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            sessionDao.clearSession()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to clear sessionDao during logout", e)
        }
        try {
            appDatabase.clearAllTables()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to clear Room database during logout", e)
        }
        try {
            sessionManager.clearAuth()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to clear session during logout", e)
        }
    }
}
