package uz.farzandim.parent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import uz.farzandim.parent.data.local.dao.*
import uz.farzandim.parent.data.local.entity.*

@Database(
    entities = [
        ChildEntity::class,
        ParentGradeEntity::class,
        ParentScheduleEntity::class,
        HolidayEntity::class,
        AnnouncementEntity::class,
        MenuEntity::class,
        ClubEntity::class,
        BookEntity::class,
        SyncQueueEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class ParentDatabase : RoomDatabase() {
    abstract fun childDao(): ChildDao
    abstract fun gradeDao(): ParentGradeDao
    abstract fun scheduleDao(): ParentScheduleDao
    abstract fun holidayDao(): HolidayDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun menuDao(): MenuDao
    abstract fun clubDao(): ClubDao
    abstract fun bookDao(): BookDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        // parent_schedules is only a cache of the backend; it now holds one schedule per week,
        // so the old mixed-period rows are dropped and refetched.
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `parent_schedules`")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `parent_schedules` (
                        `id` INTEGER NOT NULL,
                        `classId` INTEGER NOT NULL,
                        `weekStart` TEXT NOT NULL,
                        `dayOfWeek` INTEGER NOT NULL,
                        `lessonNumber` INTEGER NOT NULL,
                        `subjectId` INTEGER NOT NULL,
                        `subjectName` TEXT NOT NULL,
                        `teacherName` TEXT,
                        `room` TEXT,
                        `startTime` TEXT,
                        `endTime` TEXT,
                        `topicName` TEXT,
                        `homework` TEXT,
                        PRIMARY KEY(`classId`, `weekStart`, `dayOfWeek`, `lessonNumber`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `clubs_new` (
                        `id` INTEGER NOT NULL,
                        `studentId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT,
                        `teacherName` TEXT,
                        `status` TEXT,
                        `scheduleText` TEXT,
                        PRIMARY KEY(`id`, `studentId`)
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT OR IGNORE INTO `clubs_new` (`id`, `studentId`, `name`, `description`, `teacherName`, `status`, `scheduleText`)
                    SELECT `id`, `studentId`, `name`, `description`, `teacherName`, `status`, `scheduleText` FROM `clubs`
                """.trimIndent())
                db.execSQL("DROP TABLE `clubs`")
                db.execSQL("ALTER TABLE `clubs_new` RENAME TO `clubs`")
            }
        }
    }
}
