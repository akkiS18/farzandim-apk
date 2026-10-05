package uz.farzandim.parent.data.model

import com.google.gson.annotations.SerializedName

data class StudentChildDto(
    @SerializedName("id") val id: Int,
    @SerializedName("student_id") val studentId: Int? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("middle_name") val middleName: String? = null,
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("class_name") val className: String? = null,
    @SerializedName("class_level") val classLevel: Int? = null,
    @SerializedName("level") val level: Int? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("birthdate") val birthdate: String? = null,
    @SerializedName("ina") val ina: String? = null,
    @SerializedName("balance") val balance: Double? = null
)

data class GradeItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("class_name") val className: String? = null,
    @SerializedName("subject_id") val subjectId: Int? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("teacher_id") val teacherId: Int? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("numeric_value") val numericValue: Double? = null,
    @SerializedName("grade_date") val gradeDate: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("approved_by_parent") val approvedByParent: Boolean = false,
    @SerializedName("grade_type") val gradeType: String? = "MASTERY",
    @SerializedName("grade_category") val gradeCategory: String? = "DAILY",
    @SerializedName("lesson_number") val lessonNumber: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ClassScheduleItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("class_id") val classId: Int,
    @SerializedName("day_of_week") val dayOfWeek: Int,
    @SerializedName("lesson_number") val lessonNumber: Int,
    @SerializedName("subject_id") val subjectId: Int,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("teacher_id") val teacherId: Int? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("room") val room: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("topic_name") val topicName: String? = null,
    @SerializedName("homework") val homework: String? = null
)

data class HolidayDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String? = null,
    @SerializedName("holiday_date") val holidayDate: String? = null,
    @SerializedName("target_classes") val targetClasses: List<Int>? = null,
    @SerializedName("target_levels") val targetLevels: List<Int>? = null
)

data class PollOptionDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("option_text") val optionText: String? = null,
    @SerializedName("text") val textFallback: String? = null,
    @SerializedName("vote_count") val voteCount: Int? = null,
    @SerializedName("votes_count") val votesCountFallback: Int? = null,
    @SerializedName("percentage") val percentage: Double? = null,
    @SerializedName("user_voted") val userVoted: Boolean? = null
) {
    val text: String
        get() = optionText ?: textFallback ?: ""
    val votesCount: Int
        get() = voteCount ?: votesCountFallback ?: 0
}

data class AnnouncementDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("author_name") val authorName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("is_poll") val isPoll: Boolean = false,
    @SerializedName("poll_options") val pollOptions: List<PollOptionDto>? = null,
    @SerializedName("user_voted_option_id") val userVotedOptionId: Int? = null,
    @SerializedName("image_urls") val imageUrls: List<String>? = null
)

data class VotePollRequest(
    @SerializedName("option_id") val optionId: Int
)

data class MenuIntervalDto(
    @SerializedName("name") val name: String,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null
)

data class MenuDishDto(
    @SerializedName("name") val name: String,
    @SerializedName("composition") val composition: String? = null,
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("weight") val weight: String? = null
)

data class MealSectionDto(
    @SerializedName("interval_name") val intervalName: String,
    @SerializedName("dishes") val dishes: List<MenuDishDto> = emptyList()
)

data class DailyMenuResponse(
    @SerializedName("menu_date") val menuDate: String,
    @SerializedName("meals") val meals: List<MealSectionDto> = emptyList(),
    @SerializedName("notes") val notes: String? = null
)

data class CreateMenuCommentRequest(
    @SerializedName("menu_date") val menuDate: String,
    @SerializedName("content") val content: String
)

data class CreateGradeCommentRequest(
    @SerializedName("content") val content: String
)

data class CommentItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("grade_id") val gradeId: Int? = null,
    @SerializedName("parent_id") val parentId: Int? = null,
    @SerializedName("author_id") val authorId: Int? = null,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("type") val type: String? = null, // "GRADE" or "MENU"
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("grade_value") val gradeValue: String? = null
)

data class ClubDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("status") val status: String? = null, // "APPROVED", "PENDING", "NONE"
    @SerializedName("schedule_text") val scheduleText: String? = null
)

data class BookDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("file_url") val fileUrl: String? = null,
    @SerializedName("level") val level: Int? = null,
    @SerializedName("category_name") val categoryName: String? = null
)

data class NextChargeDto(
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("charge_date") val chargeDate: String? = null
)

data class TransactionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("amount") val amount: Double? = 0.0,
    @SerializedName("type") val type: String? = null, // "DEPOSIT", "FEE"
    @SerializedName("description") val description: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class AiReportDto(
    @SerializedName("id") val id: Int,
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("strengths") val strengths: List<String>? = null,
    @SerializedName("areas_to_improve") val areasToImprove: List<String>? = null,
    @SerializedName("recommendations") val recommendations: List<String>? = null,
    @SerializedName("week_start") val weekStart: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
