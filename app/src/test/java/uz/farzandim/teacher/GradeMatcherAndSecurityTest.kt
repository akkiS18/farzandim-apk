package uz.farzandim.teacher

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import uz.farzandim.teacher.data.local.entity.GradeEntity
import uz.farzandim.teacher.data.model.GradeResponse
import uz.farzandim.teacher.data.sync.GradeMatcher
import uz.farzandim.teacher.util.AppLogger
import uz.farzandim.teacher.util.SecurityHelper

class GradeMatcherAndSecurityTest {

    @Before
    fun setUp() {
        SecurityHelper.resetAttempts()
    }

    @Test
    fun `GradeMatcher matches grades across different dates and categories without collision`() {
        val localDay1 = GradeEntity(
            localId = "local-day1",
            studentId = 69,
            subjectId = 5,
            lessonNumber = 2,
            gradeDate = "2026-10-20",
            gradeType = "MASTERY",
            gradeCategory = "DAILY",
            value = "4",
            syncStatus = "PENDING_UPSERT"
        )
        val localDay2 = GradeEntity(
            localId = "local-day2",
            studentId = 69,
            subjectId = 5,
            lessonNumber = 2,
            gradeDate = "2026-10-21",
            gradeType = "MASTERY",
            gradeCategory = "DAILY",
            value = "5",
            syncStatus = "PENDING_UPSERT"
        )
        val localExamDay2 = GradeEntity(
            localId = "local-exam-day2",
            studentId = 69,
            subjectId = 5,
            lessonNumber = 2,
            gradeDate = "2026-10-21",
            gradeType = "MASTERY",
            gradeCategory = "QUARTERLY_EXAM",
            value = "5",
            syncStatus = "PENDING_UPSERT"
        )

        val candidates = listOf(localDay1, localDay2, localExamDay2)
        val matchedIds = mutableSetOf<String>()

        // Server returns in reverse order with ISO timestamps
        val remoteExamDay2 = GradeResponse(
            id = 1003,
            studentId = 69,
            subjectId = 5,
            lessonNumber = 2,
            gradeDate = "2026-10-21T00:00:00Z",
            gradeType = "DAILY",
            gradeCategory = "QUARTERLY_EXAM",
            value = "5"
        )
        val remoteDay2 = GradeResponse(
            id = 1002,
            studentId = 69,
            subjectId = 5,
            lessonNumber = 2,
            gradeDate = "2026-10-21T00:00:00Z",
            gradeType = "MASTERY",
            gradeCategory = "DAILY",
            value = "5"
        )
        val remoteDay1 = GradeResponse(
            id = 1001,
            studentId = 69,
            subjectId = 5,
            lessonNumber = 2,
            gradeDate = "2026-10-20T00:00:00Z",
            gradeType = "MASTERY",
            gradeCategory = "DAILY",
            value = "4"
        )

        val m1 = GradeMatcher.findMatch(remoteExamDay2, candidates, matchedIds)
        val m2 = GradeMatcher.findMatch(remoteDay2, candidates, matchedIds)
        val m3 = GradeMatcher.findMatch(remoteDay1, candidates, matchedIds)

        assertEquals("local-exam-day2", m1?.localId)
        assertEquals("local-day2", m2?.localId)
        assertEquals("local-day1", m3?.localId)
        assertEquals(3, matchedIds.size)
    }

    @Test
    fun `SecurityHelper hashes PIN with random salt and PBKDF2 and verifies accurately`() {
        val hash1 = SecurityHelper.hashPin("1234")
        val hash2 = SecurityHelper.hashPin("1234")

        assertTrue(hash1.startsWith("pbkdf2:"))
        assertNotEquals("Salts must be unique per hash", hash1, hash2)
        assertTrue(SecurityHelper.verifyPin("1234", hash1))
        assertTrue(SecurityHelper.verifyPin("1234", hash2))
        assertFalse(SecurityHelper.verifyPin("0000", hash1))
    }

    @Test
    fun `SecurityHelper enforces lockout after 5 failed attempts`() {
        val now = 1_000_000L
        assertFalse(SecurityHelper.isLockedOut(now))

        repeat(4) {
            assertFalse(SecurityHelper.recordFailedAttempt(now))
            assertFalse(SecurityHelper.isLockedOut(now))
        }

        // 5th failed attempt triggers lockout
        assertTrue(SecurityHelper.recordFailedAttempt(now))
        assertTrue(SecurityHelper.isLockedOut(now + 1000))
        assertEquals(29L, SecurityHelper.getRemainingLockoutSeconds(now + 1000))
        assertFalse(SecurityHelper.isLockedOut(now + 31_000))
    }

    @Test
    fun `AppLogger redacts passwords and tokens`() {
        val raw = """{"phone":"+998901234567","password":"mySecretPassword123","token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"}"""
        val redacted = AppLogger.redact(raw) ?: ""
        assertFalse(redacted.contains("mySecretPassword123"))
        assertFalse(redacted.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"))
        assertTrue(redacted.contains("[REDACTED]"))
    }
}
