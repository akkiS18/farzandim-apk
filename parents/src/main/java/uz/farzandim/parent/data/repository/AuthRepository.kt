package uz.farzandim.parent.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import uz.farzandim.parent.data.local.ParentDatabase
import uz.farzandim.parent.data.local.SessionManager
import uz.farzandim.parent.data.model.LoginRequest
import uz.farzandim.parent.data.model.LoginResponse
import uz.farzandim.parent.data.model.SchoolDto
import uz.farzandim.parent.data.remote.ApiService

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val gson: Gson,
    private val parentDatabase: ParentDatabase
) {
    suspend fun getSchools(): Result<List<SchoolDto>> {
        return try {
            val response = apiService.getSchools()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Maktablar ro'yxatini yuklab bo'lmadi: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithPassport(passport: String, password: String): Result<LoginResponse> {
        return try {
            val cleanPassport = passport.trim().uppercase()
            val request = LoginRequest(
                documentNo = cleanPassport,
                password = password
            )
            // Do NOT pass X-School-ID — backend resolves tenant via Origin header
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                // Verify user is PARENT role
                if (!data.user.role.equals("PARENT", ignoreCase = true)) {
                    return Result.failure(Exception("Ushbu ilova faqat ota-onalar uchun mo'ljallangan. Sizning rolingiz: ${data.user.role}"))
                }

                sessionManager.saveTokens(data.token, data.refreshToken)
                sessionManager.saveUser(
                    id = data.user.id,
                    firstName = data.user.firstName,
                    lastName = data.user.lastName,
                    role = data.user.role,
                    schoolId = data.user.schoolId,
                    passport = data.user.passport ?: cleanPassport
                )
                Result.success(data)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithPhone(phone: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(
                phone = phone.trim(),
                password = password
            )
            // Do NOT pass X-School-ID — backend resolves tenant via Origin header
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                if (!data.user.role.equals("PARENT", ignoreCase = true)) {
                    return Result.failure(Exception("Ushbu ilova faqat ota-onalar uchun mo'ljallangan. Sizning rolingiz: ${data.user.role}"))
                }

                sessionManager.saveTokens(data.token, data.refreshToken)
                sessionManager.saveUser(
                    id = data.user.id,
                    firstName = data.user.firstName,
                    lastName = data.user.lastName,
                    role = data.user.role,
                    schoolId = data.user.schoolId,
                    passport = data.user.passport
                )
                Result.success(data)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Tizimga kirishda xatolik yuz berdi"
        return try {
            val json = JSONObject(errorBody)
            when {
                json.has("error") -> json.getString("error")
                json.has("message") -> json.getString("message")
                else -> errorBody
            }
        } catch (_: Exception) {
            errorBody
        }
    }

    suspend fun setPin(pin: String) = sessionManager.setPin(pin)
    suspend fun verifyPin(pin: String): Boolean = sessionManager.verifyPin(pin)
    suspend fun setBiometricEnabled(enabled: Boolean) = sessionManager.setBiometricEnabled(enabled)
    suspend fun setLanguage(language: String) = sessionManager.setLanguage(language)
    suspend fun saveSubdomain(subdomain: String) = sessionManager.saveSubdomain(subdomain)
    suspend fun saveBaseUrl(url: String) = sessionManager.saveBaseUrl(url)

    suspend fun selectSchool(school: SchoolDto) {
        sessionManager.saveSelectedSchool(school.id, school.name)
        val sub = school.subdomain ?: school.id
        sessionManager.saveSubdomain(sub)
    }
    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            parentDatabase.clearAllTables()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to clear parent database during logout", e)
        }
        try {
            sessionManager.clearSession()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to clear session during logout", e)
        }
    }
}
