package uz.farzandim.parent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 4,
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
}
