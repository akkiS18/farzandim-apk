package uz.farzandim.teacher.data.model

import com.google.gson.annotations.SerializedName

data class ClassItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("is_main_teacher") val isMainTeacher: Boolean = false,
    @SerializedName("subject_id") val subjectId: Int? = null,
    @SerializedName("subject_name") val subjectName: String? = null
)

data class SubjectItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class LessonPlanDto(
    @SerializedName("id") val id: Int,
    @SerializedName("topic_name") val topicName: String,
    @SerializedName("lesson_number") val lessonNumber: Int? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("day_of_week") val dayOfWeek: Int? = null,
    @SerializedName("homework") val homework: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class AdjacentLessonDto(
    @SerializedName("date") val date: String,
    @SerializedName("lesson_number") val lessonNumber: Int,
    @SerializedName("subject_id") val subjectId: Int? = null
)

data class JournalAdjacentResponse(
    @SerializedName("lesson") val lesson: AdjacentLessonDto? = null
)

data class SaveLessonPlanRequest(
    @SerializedName("class_id") val classId: Int,
    @SerializedName("subject_id") val subjectId: Int,
    @SerializedName("day_of_week") val dayOfWeek: Int,
    @SerializedName("lesson_number") val lessonNumber: Int,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("topic_name") val topicName: String,
    @SerializedName("homework") val homework: String,
    @SerializedName("notes") val notes: String = ""
)

data class SaveLessonPlanResponse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("message") val message: String? = null
)

data class ClassScheduleItemDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("day_of_week") val dayOfWeek: Int,
    @SerializedName("lesson_number") val lessonNumber: Int,
    @SerializedName("subject_id") val subjectId: Int,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("room") val room: String? = null
)

data class JournalLessonItem(
    val subjectId: Int,
    val subjectName: String,
    val lessonNumber: Int
)

data class StudentItem(
    @SerializedName("id") val id: Int,
    @SerializedName("student_id") val studentId: Int? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("middle_name") val middleName: String? = null,
    @SerializedName("enrollment_date") val enrollmentDate: String? = null
) {
    val realStudentId: Int
        get() = studentId ?: id
}

data class GradingSystemOption(
    @SerializedName("label") val label: String,
    @SerializedName("numeric_value") val numericValue: Double? = null
)

data class GradingSystemItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("min_value") val minValue: Double? = null,
    @SerializedName("max_value") val maxValue: Double? = null,
    @SerializedName("options") val options: Any? = null,
    @SerializedName("is_active") val isActive: Boolean = false
)

data class CreateGradeRequest(
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("subject_id") val subjectId: Int,
    @SerializedName("value") val value: String,
    @SerializedName("grade_date") val gradeDate: String? = null,
    @SerializedName("grading_system_id") val gradingSystemId: Int? = null,
    @SerializedName("grade_type") val gradeType: String? = "MASTERY",
    @SerializedName("grade_category") val gradeCategory: String? = "REGULAR",
    @SerializedName("lesson_number") val lessonNumber: Int? = null
)

data class BatchCreateGradesRequest(
    @SerializedName("grades") val grades: List<CreateGradeRequest>
)

data class GradeResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("class_name") val className: String? = null,
    @SerializedName("subject_id") val subjectId: Int,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("teacher_id") val teacherId: Int? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("value") val value: String,
    @SerializedName("numeric_value") val numericValue: Double? = null,
    @SerializedName("grade_date") val gradeDate: String,
    @SerializedName("status") val status: String? = "approved",
    @SerializedName("approved_by_parent") val approvedByParent: Boolean = false,
    @SerializedName("grading_system_id") val gradingSystemId: Int? = null,
    @SerializedName("grade_type") val gradeType: String? = "MASTERY",
    @SerializedName("grade_category") val gradeCategory: String? = "REGULAR",
    @SerializedName("lesson_number") val lessonNumber: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ChangeGradeStatusRequest(
    @SerializedName("mark_uids") val markUids: List<Int>,
    @SerializedName("status") val status: String,
    @SerializedName("grade_ids") val gradeIds: List<Int> = markUids
)
