package uz.farzandim.teacher.data.model

import com.google.gson.annotations.SerializedName

data class TodayLessonItem(
    @SerializedName("schedule_id") val scheduleId: Int? = null,
    @SerializedName("class_id") val classId: Int,
    @SerializedName("class_name") val className: String,
    @SerializedName("subject_id") val subjectId: Int,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("lesson_number") val lessonNumber: Int,
    @SerializedName("time") val time: String? = null,
    @SerializedName("is_fully_marked") val isFullyMarked: Boolean = false,
    @SerializedName("is_marked") val isMarked: Boolean = false,
    @SerializedName("marked_students_count") val markedStudentsCount: Int = 0,
    @SerializedName("total_students_count") val totalStudentsCount: Int = 0
)

data class TodayLessonsResponse(
    @SerializedName("date") val date: String? = null,
    @SerializedName("is_holiday") val isHoliday: Boolean = false,
    @SerializedName("is_weekend") val isWeekend: Boolean = false,
    @SerializedName("holiday_name") val holidayName: String? = null,
    @SerializedName("pending_count") val pendingCount: Int = 0,
    @SerializedName("completed_count") val completedCount: Int = 0,
    @SerializedName("lessons") val lessons: List<TodayLessonItem> = emptyList()
)
