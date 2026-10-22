package uz.farzandim.teacher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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
}
