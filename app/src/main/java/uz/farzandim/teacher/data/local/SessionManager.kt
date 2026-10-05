package uz.farzandim.teacher.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import uz.farzandim.teacher.util.SecurityHelper

private val Context.dataStore by preferencesDataStore(name = "farzandim_teacher_prefs")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_SCHOOL_ID = stringPreferencesKey("selected_school_id")
        val KEY_SCHOOL_NAME = stringPreferencesKey("selected_school_name")
        val KEY_TOKEN = stringPreferencesKey("auth_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_USER_ID = intPreferencesKey("user_id")
        val KEY_USER_FIRST_NAME = stringPreferencesKey("user_first_name")
        val KEY_USER_LAST_NAME = stringPreferencesKey("user_last_name")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")

        val KEY_SUBDOMAIN = stringPreferencesKey("custom_subdomain")
        val KEY_BASE_URL = stringPreferencesKey("custom_base_url")

        val KEY_PIN_HASH = stringPreferencesKey("user_pin_hash")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val KEY_LAST_SYNCED_AT = longPreferencesKey("last_synced_at")

        const val DEFAULT_SUBDOMAIN = "test_school"
        val DEFAULT_BASE_URL: String = uz.farzandim.teacher.BuildConfig.BASE_URL

        fun formatOrigin(subdomain: String): String {
            val clean = subdomain.trim().lowercase()
            return "https://$clean.akademx.uz"
        }

        fun formatBaseUrl(subdomain: String = ""): String {
            return DEFAULT_BASE_URL
        }

        fun formatSubdomainToSchoolName(subdomain: String): String {
            val clean = subdomain.trim()
            if (clean.isBlank()) return "Maktab"
            if (clean.equals("test_school", ignoreCase = true) || clean.equals("test", ignoreCase = true)) {
                return "Test Maktab"
            }
            val parts = clean.split('_', '-').filter { it.isNotBlank() }
            val formattedParts = parts.map { part ->
                if (part.all { it.isDigit() }) part
                else part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() }
            }
            val combined = formattedParts.joinToString(" ")
            return if (!combined.contains("maktab", ignoreCase = true) &&
                !combined.contains("school", ignoreCase = true) &&
                !combined.contains("litsey", ignoreCase = true) &&
                !combined.contains("gimnaziya", ignoreCase = true)
            ) {
                "$combined Maktabi"
            } else {
                combined
            }
        }
    }

    val schoolIdFlow: Flow<String?> = context.dataStore.data.map { it[KEY_SCHOOL_ID] }
    val schoolNameFlow: Flow<String?> = context.dataStore.data.map { it[KEY_SCHOOL_NAME] }
    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    val subdomainFlow: Flow<String> = context.dataStore.data.map {
        it[KEY_SUBDOMAIN] ?: DEFAULT_SUBDOMAIN
    }

    val baseUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val customUrl = prefs[KEY_BASE_URL]
        if (!customUrl.isNullOrBlank() && !customUrl.contains(".akademx.uz:6560")) {
            customUrl
        } else {
            DEFAULT_BASE_URL
        }
    }

    val userFullNameFlow: Flow<String> = context.dataStore.data.map {
        val first = it[KEY_USER_FIRST_NAME] ?: ""
        val last = it[KEY_USER_LAST_NAME] ?: ""
        "$first $last".trim().ifEmpty { "O'qituvchi" }
    }

    val isPinSetFlow: Flow<Boolean> = context.dataStore.data.map {
        !it[KEY_PIN_HASH].isNullOrBlank()
    }

    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_BIOMETRIC_ENABLED] ?: false
    }

    val lastSyncedAtFlow: Flow<Long?> = context.dataStore.data.map {
        it[KEY_LAST_SYNCED_AT]
    }

    suspend fun saveLastSyncedAt(timestamp: Long) {
        context.dataStore.edit {
            it[KEY_LAST_SYNCED_AT] = timestamp
        }
    }

    suspend fun getLastSyncedAt(): Long? {
        return context.dataStore.data.first()[KEY_LAST_SYNCED_AT]
    }

    suspend fun saveSelectedSchool(schoolId: String, schoolName: String) {
        context.dataStore.edit {
            it[KEY_SCHOOL_ID] = schoolId
            it[KEY_SCHOOL_NAME] = schoolName
        }
    }

    suspend fun saveSubdomain(subdomain: String) {
        val clean = subdomain.trim().lowercase()
        val currentSub = getSubdomain()
        context.dataStore.edit {
            it[KEY_SUBDOMAIN] = clean
            it[KEY_BASE_URL] = DEFAULT_BASE_URL
            it[KEY_SCHOOL_NAME] = formatSubdomainToSchoolName(clean)
            if (clean != currentSub) {
                it.remove(KEY_SCHOOL_ID)
            }
        }
    }

    suspend fun getSubdomain(): String {
        return context.dataStore.data.first()[KEY_SUBDOMAIN] ?: DEFAULT_SUBDOMAIN
    }

    suspend fun getSchoolName(): String? {
        return context.dataStore.data.first()[KEY_SCHOOL_NAME]
    }

    suspend fun saveAuth(
        token: String,
        refreshToken: String?,
        userId: Int,
        firstName: String,
        lastName: String,
        role: String,
        schoolId: String? = null
    ) {
        context.dataStore.edit {
            it[KEY_TOKEN] = token
            refreshToken?.let { rt -> it[KEY_REFRESH_TOKEN] = rt }
            it[KEY_USER_ID] = userId
            it[KEY_USER_FIRST_NAME] = firstName
            it[KEY_USER_LAST_NAME] = lastName
            it[KEY_USER_ROLE] = role
            schoolId?.let { sid -> it[KEY_SCHOOL_ID] = sid }
        }
    }

    suspend fun clearAuth() {
        try {
            androidx.work.WorkManager.getInstance(context).cancelUniqueWork("GRADE_SYNC_WORK_IMMEDIATE")
            androidx.work.WorkManager.getInstance(context).cancelUniqueWork("GRADE_SYNC_WORK_PERIODIC")
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to cancel work manager jobs", e)
        }

        try {
            context.dataStore.edit {
                it.remove(KEY_TOKEN)
                it.remove(KEY_REFRESH_TOKEN)
                it.remove(KEY_USER_ID)
                it.remove(KEY_USER_FIRST_NAME)
                it.remove(KEY_USER_LAST_NAME)
                it.remove(KEY_USER_ROLE)
                it.remove(KEY_PIN_HASH)
                it.remove(KEY_BIOMETRIC_ENABLED)
                it.remove(KEY_SCHOOL_ID)
                // Keep KEY_SUBDOMAIN and KEY_SCHOOL_NAME for login screen prefill
            }
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to clear dataStore prefs", e)
        }
    }

    // PIN Management
    suspend fun savePin(pin: String) {
        val hash = SecurityHelper.hashPin(pin)
        context.dataStore.edit {
            it[KEY_PIN_HASH] = hash
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val storedHash = context.dataStore.data.first()[KEY_PIN_HASH]
        return SecurityHelper.verifyPin(pin, storedHash)
    }

    suspend fun isPinSet(): Boolean {
        val storedHash = context.dataStore.data.first()[KEY_PIN_HASH]
        return !storedHash.isNullOrBlank()
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun isBiometricEnabled(): Boolean {
        return context.dataStore.data.first()[KEY_BIOMETRIC_ENABLED] ?: false
    }

    suspend fun getSchoolId(): String? = context.dataStore.data.first()[KEY_SCHOOL_ID]
    suspend fun getToken(): String? = context.dataStore.data.first()[KEY_TOKEN]
    suspend fun getRefreshToken(): String? = context.dataStore.data.first()[KEY_REFRESH_TOKEN]

    suspend fun saveTokens(token: String, refreshToken: String?) {
        context.dataStore.edit {
            it[KEY_TOKEN] = token
            if (refreshToken != null) {
                it[KEY_REFRESH_TOKEN] = refreshToken
            }
        }
    }

    suspend fun getBaseUrl(): String {
        val prefs = context.dataStore.data.first()
        val customUrl = prefs[KEY_BASE_URL]
        if (!customUrl.isNullOrBlank() && !customUrl.contains(".akademx.uz:6560")) {
            return customUrl
        }
        return DEFAULT_BASE_URL
    }

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit {
            it[KEY_BASE_URL] = url
        }
    }
}
