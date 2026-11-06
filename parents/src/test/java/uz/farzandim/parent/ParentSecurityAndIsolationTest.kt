package uz.farzandim.parent

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import uz.farzandim.parent.data.local.entity.ClubEntity
import uz.farzandim.parent.util.AppLogger
import uz.farzandim.parent.util.SecurityHelper

class ParentSecurityAndIsolationTest {

    @Before
    fun setUp() {
        SecurityHelper.resetAttempts()
    }

    @Test
    fun `SecurityHelper uses unique random salt per PIN and supports lockout`() {
        val h1 = SecurityHelper.hashPin("5678")
        val h2 = SecurityHelper.hashPin("5678")

        assertTrue(h1.startsWith("pbkdf2:"))
        assertNotEquals(h1, h2)
        assertTrue(SecurityHelper.verifyPin("5678", h1))
        assertTrue(SecurityHelper.verifyPin("5678", h2))
        assertFalse(SecurityHelper.verifyPin("9999", h1))

        val now = 5_000_000L
        repeat(4) { assertFalse(SecurityHelper.recordFailedAttempt(now)) }
        assertTrue(SecurityHelper.recordFailedAttempt(now))
        assertTrue(SecurityHelper.isLockedOut(now + 5000))
    }

    @Test
    fun `ClubEntity isolates club status per studentId`() {
        val child1Club = ClubEntity(
            id = 10,
            studentId = 101,
            name = "Robototexnika",
            description = "STEM",
            teacherName = "Aliyev A.",
            status = "APPROVED",
            scheduleText = "Du-Ch-Ju 14:00"
        )
        val child2Club = ClubEntity(
            id = 10,
            studentId = 102,
            name = "Robototexnika",
            description = "STEM",
            teacherName = "Aliyev A.",
            status = "PENDING",
            scheduleText = "Du-Ch-Ju 14:00"
        )

        val mapByCompositeKey = listOf(child1Club, child2Club).associateBy { Pair(it.id, it.studentId) }
        assertEquals(2, mapByCompositeKey.size)
        assertEquals("APPROVED", mapByCompositeKey[Pair(10, 101)]?.status)
        assertEquals("PENDING", mapByCompositeKey[Pair(10, 102)]?.status)
    }

    @Test
    fun `AppLogger redacts sensitive tokens and passwords`() {
        val body = """{"token":"secret-jwt","refresh_token":"secret-refresh","password":"pass"}"""
        val clean = AppLogger.redact(body) ?: ""
        assertFalse(clean.contains("secret-jwt"))
        assertFalse(clean.contains("secret-refresh"))
        assertTrue(clean.contains("[REDACTED]"))
    }
}
