package uz.farzandim.parent.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import uz.farzandim.parent.data.local.SessionManager
import uz.farzandim.parent.data.model.RefreshTokenRequest
import uz.farzandim.parent.data.model.RefreshTokenResponse
import uz.farzandim.parent.util.AppLogger

class TenantInterceptor(private val sessionManager: SessionManager) : Interceptor {
    // Paths where tenant is resolved exclusively by Origin header (not X-School-ID)
    private val excludedPaths = listOf(
        "api/schools/login",
        "api/schools/ping",
        "api/admin/schools"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        // 1. Origin & Referer headers to simulate web browser context
        val subdomain = runBlocking { sessionManager.getSubdomain() }
        val origin = SessionManager.formatOrigin(subdomain)

        if (original.header("Origin").isNullOrEmpty()) {
            builder.header("Origin", origin)
        }
        if (original.header("Referer").isNullOrEmpty()) {
            builder.header("Referer", "$origin/")
        }

        // 2. X-School-ID header — skip for login/admin/ping paths,
        //    because the backend resolves tenant from Origin during those calls.
        val requestPath = original.url.encodedPath
        val isExcluded = excludedPaths.any { requestPath.contains(it) }

        if (!isExcluded) {
            val existingSchoolId = original.header("X-School-ID")
            if (existingSchoolId.isNullOrEmpty()) {
                val savedSchoolId = runBlocking { sessionManager.getSchoolId() }
                if (!savedSchoolId.isNullOrEmpty()) {
                    builder.header("X-School-ID", savedSchoolId)
                }
            }
        }

        return chain.proceed(builder.build())
    }
}

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        val existingAuth = original.header("Authorization")
        if (existingAuth.isNullOrEmpty()) {
            val token = runBlocking { sessionManager.getToken() }
            if (!token.isNullOrEmpty()) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(builder.build())
    }
}

class DynamicBaseUrlInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val currentBaseUrl = runBlocking { sessionManager.getBaseUrl() }
        val newHttpUrl = currentBaseUrl.toHttpUrlOrNull()

        val request = if (newHttpUrl != null) {
            val newUrl = original.url.newBuilder()
                .scheme(newHttpUrl.scheme)
                .host(newHttpUrl.host)
                .port(newHttpUrl.port)
                .build()
            original.newBuilder().url(newUrl).build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}

class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val gson: Gson
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // Do not retry refresh endpoint itself to prevent infinite loop
        if (response.request.url.encodedPath.contains("api/schools/refresh")) {
            return null
        }

        // Only retry once on 401
        if (responseCount(response) >= 2) {
            return null
        }

        synchronized(this) {
            val currentToken = runBlocking { sessionManager.getToken() }
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // If token was already refreshed by another thread, retry request with new token
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = runBlocking { sessionManager.getRefreshToken() } ?: return null
            val baseUrl = runBlocking { sessionManager.getBaseUrl() }
            val refreshUrl = (if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/") + "api/schools/refresh"

            val refreshBody = gson.toJson(RefreshTokenRequest(refreshToken))
                .toRequestBody("application/json".toMediaTypeOrNull())

            val refreshRequest = Request.Builder()
                .url(refreshUrl)
                .post(refreshBody)
                .build()

            val client = OkHttpClient()
            return try {
                val refreshResponse = client.newCall(refreshRequest).execute()
                if (refreshResponse.isSuccessful) {
                    val bodyString = refreshResponse.body?.string()
                    val tokenData = gson.fromJson(bodyString, RefreshTokenResponse::class.java)
                    if (tokenData?.token != null) {
                        runBlocking {
                            sessionManager.saveTokens(tokenData.token, tokenData.refreshToken ?: refreshToken)
                        }
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${tokenData.token}")
                            .build()
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}

class NetworkDebugInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method
        val startTime = System.currentTimeMillis()

        return try {
            val response = chain.proceed(request)
            val duration = System.currentTimeMillis() - startTime
            val statusCode = response.code

            val responseBody = try {
                response.peekBody(2048).string()
            } catch (_: Exception) {
                null
            }

            AppLogger.log(
                method = method,
                url = url,
                statusCode = statusCode,
                durationMs = duration,
                responseBody = responseBody,
                error = if (!response.isSuccessful) "HTTP $statusCode: ${response.message}" else null
            )
            response
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            AppLogger.log(
                method = method,
                url = url,
                statusCode = null,
                durationMs = duration,
                error = e.message ?: e.javaClass.simpleName
            )
            throw e
        }
    }
}
