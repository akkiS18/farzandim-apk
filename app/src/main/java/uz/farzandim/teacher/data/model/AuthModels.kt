package uz.farzandim.teacher.data.model

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

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("middle_name") val middleName: String? = null,
    @SerializedName("role") val role: String,
    @SerializedName("school_id") val schoolId: String? = null,
    @SerializedName("document_no") val documentNo: String? = null
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("user") val user: UserDto
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshTokenResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refresh_token") val refreshToken: String? = null
)
