package uz.farzandim.teacher.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityHelper {
    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private const val MAX_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 30_000L

    @Volatile
    private var failedAttempts = 0

    @Volatile
    private var lockoutUntilMs = 0L

    fun hashPin(pin: String): String {
        val cleanPin = pin.trim()
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val derived = pbkdf2(cleanPin, salt, ITERATIONS)
        return "pbkdf2:${salt.toHex()}:${derived.toHex()}"
    }

    fun verifyPin(inputPin: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrEmpty()) return false
        val cleanPin = inputPin.trim()

        if (storedHash.startsWith("pbkdf2:")) {
            val parts = storedHash.split(":")
            if (parts.size != 3) return false
            val salt = parts[1].hexToBytes() ?: return false
            val expected = parts[2].hexToBytes() ?: return false
            val actual = pbkdf2(cleanPin, salt, ITERATIONS)
            return MessageDigest.isEqual(actual, expected)
        }

        // Legacy fallback for existing unsalted SHA-256 PINs
        val digest = MessageDigest.getInstance("SHA-256")
        val legacyActual = digest.digest(cleanPin.toByteArray(Charsets.UTF_8))
        val legacyExpected = storedHash.hexToBytes() ?: return false
        return MessageDigest.isEqual(legacyActual, legacyExpected)
    }

    fun isLockedOut(nowMs: Long = System.currentTimeMillis()): Boolean {
        return nowMs < lockoutUntilMs
    }

    fun getRemainingLockoutSeconds(nowMs: Long = System.currentTimeMillis()): Long {
        val rem = lockoutUntilMs - nowMs
        return if (rem > 0) (rem + 999) / 1000 else 0L
    }

    fun recordFailedAttempt(nowMs: Long = System.currentTimeMillis()): Boolean {
        failedAttempts++
        if (failedAttempts >= MAX_ATTEMPTS) {
            lockoutUntilMs = nowMs + LOCKOUT_DURATION_MS
            failedAttempts = 0
            return true
        }
        return false
    }

    fun resetAttempts() {
        failedAttempts = 0
        lockoutUntilMs = 0L
    }

    private fun pbkdf2(pin: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return try {
            val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            skf.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray? {
        if (length % 2 != 0) return null
        return try {
            ByteArray(length / 2) { i ->
                substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (_: NumberFormatException) {
            null
        }
    }
}
