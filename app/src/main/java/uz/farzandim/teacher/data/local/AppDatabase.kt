package uz.farzandim.teacher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import uz.farzandim.teacher.data.local.dao.*
import uz.farzandim.teacher.data.local.entity.*

@Database(
    entities = [
        SchoolEntity::class,
        UserSessionEntity::class,
        ClassEntity::class,
        SubjectEntity::class,
        StudentEntity::class,
        TodayLessonEntity::class,
        GradeEntity::class,
        GradeCommentEntity::class,
        SyncQueueEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao
    abstract fun sessionDao(): SessionDao
    abstract fun classDao(): ClassDao
    abstract fun subjectDao(): SubjectDao
    abstract fun studentDao(): StudentDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun gradeDao(): GradeDao
    abstract fun commentDao(): CommentDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `grades` ADD COLUMN `conflictRemoteValue` TEXT")
                    db.execSQL("ALTER TABLE `grades` ADD COLUMN `conflictRemoteId` INTEGER")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `students` ADD COLUMN `enrollmentDate` TEXT")
                } catch (_: Exception) {}
            }
        }
    }
}
