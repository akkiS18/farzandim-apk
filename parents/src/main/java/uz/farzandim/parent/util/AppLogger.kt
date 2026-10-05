package uz.farzandim.parent.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date()),
    val method: String,
    val url: String,
    val statusCode: Int? = null,
    val durationMs: Long? = null,
    val requestBody: String? = null,
    val responseBody: String? = null,
    val error: String? = null,
    val isError: Boolean = false
)

object AppLogger {
    private const val MAX_LOGS = 100
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun log(
        method: String,
        url: String,
        statusCode: Int? = null,
        durationMs: Long? = null,
        requestBody: String? = null,
        responseBody: String? = null,
        error: String? = null
    ) {
        val isErr = (statusCode != null && statusCode !in 200..299) || error != null
        val entry = LogEntry(
            method = method,
            url = url,
            statusCode = statusCode,
            durationMs = durationMs,
            requestBody = requestBody,
            responseBody = responseBody,
            error = error,
            isError = isErr
        )
        val current = _logs.value.toMutableList()
        current.add(0, entry)
        if (current.size > MAX_LOGS) {
            _logs.value = current.take(MAX_LOGS)
        } else {
            _logs.value = current
        }
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
