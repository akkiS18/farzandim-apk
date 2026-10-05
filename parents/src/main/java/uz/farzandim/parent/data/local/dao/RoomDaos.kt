package uz.farzandim.parent.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uz.farzandim.parent.data.local.entity.*

@Dao
interface ChildDao {
    @Query("SELECT * FROM children ORDER BY firstName ASC")
    fun getAllChildren(): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :studentId LIMIT 1")
    suspend fun getChildById(studentId: Int): ChildEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildren(children: List<ChildEntity>)

    @Query("DELETE FROM children")
    suspend fun clearChildren()
}

@Dao
interface ParentGradeDao {
    @Query("SELECT * FROM parent_grades WHERE studentId = :studentId ORDER BY gradeDate DESC")
    fun getGradesForStudent(studentId: Int): Flow<List<ParentGradeEntity>>

    @Query("SELECT * FROM parent_grades WHERE studentId = :studentId AND gradeDate = :date")
    suspend fun getGradesForStudentAndDate(studentId: Int, date: String): List<ParentGradeEntity>

    @Query("UPDATE parent_grades SET approvedByParent = 1 WHERE id = :gradeId")
    suspend fun markApprovedLocally(gradeId: Int)

    @Query("UPDATE parent_grades SET approvedByParent = 1 WHERE id IN (:gradeIds)")
    suspend fun markAllApprovedLocally(gradeIds: List<Int>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<ParentGradeEntity>)

    @Query("DELETE FROM parent_grades WHERE studentId = :studentId")
    suspend fun clearGradesForStudent(studentId: Int)
}

@Dao
interface ParentScheduleDao {
    @Query("SELECT * FROM parent_schedules WHERE classId = :classId ORDER BY dayOfWeek ASC, lessonNumber ASC")
    fun getScheduleForClass(classId: Int): Flow<List<ParentScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ParentScheduleEntity>)

    @Query("DELETE FROM parent_schedules WHERE classId = :classId")
    suspend fun clearScheduleForClass(classId: Int)
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY holidayDate ASC")
    fun getHolidays(): Flow<List<HolidayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidays(holidays: List<HolidayEntity>)

    @Query("DELETE FROM holidays")
    suspend fun clearHolidays()
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY createdAt DESC")
    fun getAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("UPDATE announcements SET userVotedOptionId = :optionId WHERE id = :announcementId")
    suspend fun markVotedLocally(announcementId: Int, optionId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

    @Query("DELETE FROM announcements")
    suspend fun clearAnnouncements()
}

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu_days WHERE menuDate = :date LIMIT 1")
    fun getMenuForDate(date: String): Flow<MenuEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenu(menu: MenuEntity)

    @Query("DELETE FROM menu_days")
    suspend fun clearMenu()
}

@Dao
interface ClubDao {
    @Query("SELECT * FROM clubs WHERE studentId = :studentId ORDER BY name ASC")
    fun getClubsForStudent(studentId: Int): Flow<List<ClubEntity>>

    @Query("UPDATE clubs SET status = :status WHERE id = :clubId AND studentId = :studentId")
    suspend fun updateClubStatusLocally(clubId: Int, studentId: Int, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClubs(clubs: List<ClubEntity>)

    @Query("DELETE FROM clubs WHERE studentId = :studentId")
    suspend fun clearClubsForStudent(studentId: Int)
}

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getBooks(): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Query("DELETE FROM books")
    suspend fun clearBooks()
}

@Dao
interface SyncQueueDao {
    @Insert
    suspend fun enqueue(item: SyncQueueEntity): Long

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getPendingItems(): List<SyncQueueEntity>

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun dequeue(id: Long)

    @Query("UPDATE sync_queue SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetry(id: Long)

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getQueueCount(): Flow<Int>
}
