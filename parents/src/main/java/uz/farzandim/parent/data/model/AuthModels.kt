package uz.farzandim.parent.data.model

import com.google.gson.annotations.SerializedName

data class SchoolDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("subdomain") val subdomain: String? = null,
    @SerializedName("db_connection_string") val dbConnectionString: String? = null
)

data class LoginRequest(
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("document_no") val documentNo: String? = null,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("user") val user: UserDto
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("role") val role: String,
    @SerializedName("school_id") val schoolId: String,
    @SerializedName("passport") val passport: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("password_reset_required") val passwordResetRequired: Boolean = false
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshTokenResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refresh_token") val refreshToken: String? = null
)
