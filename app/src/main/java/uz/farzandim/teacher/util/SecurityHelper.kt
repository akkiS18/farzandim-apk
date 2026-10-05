package uz.farzandim.teacher.util

import java.security.MessageDigest

object SecurityHelper {

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(inputPin: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrEmpty()) return false
        val inputHash = hashPin(inputPin.trim())
        return inputHash.equals(storedHash, ignoreCase = true)
    }
}
