package uz.farzandim.teacher.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.farzandim.teacher.data.local.dao.ScheduleDao
import uz.farzandim.teacher.data.local.entity.TodayLessonEntity
import uz.farzandim.teacher.data.model.TodayLessonsResponse
import uz.farzandim.teacher.data.remote.ApiService

class ScheduleRepository(
    private val apiService: ApiService,
    private val scheduleDao: ScheduleDao
) {

    private val _scheduleMeta = MutableStateFlow<TodayLessonsResponse?>(null)
    val scheduleMeta: StateFlow<TodayLessonsResponse?> = _scheduleMeta.asStateFlow()

    fun getTodayLessonsFlow(date: String): Flow<List<TodayLessonEntity>> {
        return scheduleDao.getTodayLessonsFlow(date)
    }

    suspend fun refreshTodayLessons(date: String): Result<List<TodayLessonEntity>> {
        return try {
            val response = apiService.getTeacherTodayLessons(date)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val entities = data.lessons.map { item ->
                    TodayLessonEntity(
                        id = "${date}_${item.classId}_${item.subjectId}_${item.lessonNumber}",
                        date = date,
                        scheduleId = item.scheduleId,
                        classId = item.classId,
                        className = item.className,
                        subjectId = item.subjectId,
                        subjectName = item.subjectName,
                        lessonNumber = item.lessonNumber,
                        time = item.time,
                        isFullyMarked = item.isFullyMarked,
                        isMarked = item.isMarked,
                        markedStudentsCount = item.markedStudentsCount,
                        totalStudentsCount = item.totalStudentsCount
                    )
                }

                scheduleDao.clearTodayLessons(date)
                scheduleDao.insertTodayLessons(entities)
                _scheduleMeta.value = data
                Result.success(entities)
            } else {
                val cached = scheduleDao.getTodayLessons(date)
                if (cached.isNotEmpty()) {
                    Result.success(cached)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Dars jadvalini yuklab bo'lmadi"))
                }
            }
        } catch (e: Exception) {
            val cached = scheduleDao.getTodayLessons(date)
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }
}
