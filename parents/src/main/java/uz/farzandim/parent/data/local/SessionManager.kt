package uz.farzandim.parent.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import uz.farzandim.parent.util.SecurityHelper

private val Context.dataStore by preferencesDataStore(name = "farzandim_parent_prefs")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_SCHOOL_ID = stringPreferencesKey("selected_school_id")
        val KEY_SCHOOL_NAME = stringPreferencesKey("selected_school_name")
        val KEY_TOKEN = stringPreferencesKey("auth_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_USER_ID = intPreferencesKey("user_id")
        val KEY_USER_FIRST_NAME = stringPreferencesKey("user_first_name")
        val KEY_USER_LAST_NAME = stringPreferencesKey("user_last_name")
        val KEY_USER_PASSPORT = stringPreferencesKey("user_passport")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")

        val KEY_SUBDOMAIN = stringPreferencesKey("custom_subdomain")
        val KEY_BASE_URL = stringPreferencesKey("custom_base_url")

        val KEY_PIN_HASH = stringPreferencesKey("user_pin_hash")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val KEY_LANGUAGE = stringPreferencesKey("app_language")

        const val DEFAULT_SUBDOMAIN = "test_school"
        val DEFAULT_BASE_URL: String = uz.farzandim.parent.BuildConfig.BASE_URL

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
    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }

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
        "$first $last".trim().ifEmpty { "Ota-ona" }
    }

    val userPassportFlow: Flow<String?> = context.dataStore.data.map { it[KEY_USER_PASSPORT] }

    val isPinSetFlow: Flow<Boolean> = context.dataStore.data.map {
        !it[KEY_PIN_HASH].isNullOrBlank()
    }

    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_BIOMETRIC_ENABLED] ?: false
    }

    val languageFlow: Flow<String> = context.dataStore.data.map {
        it[KEY_LANGUAGE] ?: "uz"
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
        val schoolName = formatSubdomainToSchoolName(clean)
        context.dataStore.edit {
            it[KEY_SUBDOMAIN] = clean
            it[KEY_SCHOOL_NAME] = schoolName
            // If subdomain changed, clear the cached school ID so the new login
            // resolves the correct tenant rather than reusing the old one
            if (clean != currentSub) {
                it.remove(KEY_SCHOOL_ID)
            }
        }
    }

    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit {
            it[KEY_BASE_URL] = url.trim()
        }
    }

    suspend fun saveTokens(token: String, refreshToken: String?) {
        context.dataStore.edit {
            it[KEY_TOKEN] = token
            if (refreshToken != null) {
                it[KEY_REFRESH_TOKEN] = refreshToken
            }
        }
    }

    suspend fun saveUser(
        id: Int,
        firstName: String,
        lastName: String,
        role: String,
        schoolId: String,
        passport: String?
    ) {
        context.dataStore.edit {
            it[KEY_USER_ID] = id
            it[KEY_USER_FIRST_NAME] = firstName
            it[KEY_USER_LAST_NAME] = lastName
            it[KEY_USER_ROLE] = role
            it[KEY_SCHOOL_ID] = schoolId
            if (passport != null) {
                it[KEY_USER_PASSPORT] = passport
            }
        }
    }

    suspend fun setPin(pin: String) {
        val hash = SecurityHelper.hashPin(pin)
        context.dataStore.edit {
            it[KEY_PIN_HASH] = hash
        }
    }

    suspend fun verifyPin(enteredPin: String): Boolean {
        val hash = context.dataStore.data.first()[KEY_PIN_HASH] ?: return false
        return SecurityHelper.verifyPin(enteredPin, hash)
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit {
            it[KEY_LANGUAGE] = languageCode
        }
    }

    suspend fun getToken(): String? = context.dataStore.data.first()[KEY_TOKEN]
    suspend fun getRefreshToken(): String? = context.dataStore.data.first()[KEY_REFRESH_TOKEN]
    suspend fun getSchoolId(): String? = context.dataStore.data.first()[KEY_SCHOOL_ID]
    suspend fun getSubdomain(): String = context.dataStore.data.first()[KEY_SUBDOMAIN] ?: DEFAULT_SUBDOMAIN
    suspend fun getBaseUrl(): String {
        val prefs = context.dataStore.data.first()
        val customUrl = prefs[KEY_BASE_URL]
        return if (!customUrl.isNullOrBlank() && !customUrl.contains(".akademx.uz:6560")) {
            customUrl
        } else {
            DEFAULT_BASE_URL
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(KEY_TOKEN)
            it.remove(KEY_REFRESH_TOKEN)
            it.remove(KEY_USER_ID)
            it.remove(KEY_USER_FIRST_NAME)
            it.remove(KEY_USER_LAST_NAME)
            it.remove(KEY_USER_PASSPORT)
            it.remove(KEY_USER_ROLE)
            it.remove(KEY_PIN_HASH)
            it.remove(KEY_BIOMETRIC_ENABLED)
            // Clear school ID so it doesn't leak to next login on a different school
            it.remove(KEY_SCHOOL_ID)
            // Keep KEY_SUBDOMAIN and KEY_SCHOOL_NAME for login screen prefill
        }
    }
}
