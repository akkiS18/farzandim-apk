package uz.farzandim.parent.util

import java.security.MessageDigest

object SecurityHelper {
    private const val SALT = "Farzandim_Parent_Secure_Salt_2026"

    fun hashPin(pin: String): String {
        val input = "$SALT$pin$SALT"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, hash: String): Boolean {
        return hashPin(pin) == hash
    }
}
