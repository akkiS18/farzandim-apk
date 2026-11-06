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
    version = 5,
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
