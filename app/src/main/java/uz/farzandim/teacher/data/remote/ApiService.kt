package uz.farzandim.teacher.data.remote

import retrofit2.Response
import retrofit2.http.*
import uz.farzandim.teacher.data.model.*

interface ApiService {

    // 1. Schools List (Central DB Public Discovery)
    @GET("api/public/schools")
    suspend fun getSchools(): Response<List<SchoolDto>>

    // 2. School Login — tenant is resolved by backend via Origin header, NOT X-School-ID
    @POST("api/schools/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // 2b. Refresh Token
    @POST("api/schools/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    // 3. Teacher Today Lessons
    @GET("api/schools/teachers/today-lessons")
    suspend fun getTeacherTodayLessons(
        @Query("date") date: String
    ): Response<TodayLessonsResponse>

    // 4. Classes List
    @GET("api/schools/classes")
    suspend fun getClasses(): Response<List<ClassItem>>

    // 4b. Class Schedule
    @GET("api/schools/classes/{id}/schedule")
    suspend fun getClassSchedule(
        @Path("id") classId: Int,
        @Query("date") date: String? = null
    ): Response<List<ClassScheduleItemDto>>

    // 4c. Lesson Plans (Mavzu / Topic)
    @GET("api/schools/lesson-plans")
    suspend fun getLessonPlans(
        @Query("class_id") classId: Int,
        @Query("subject_id") subjectId: Int,
        @Query("start_date_from") startDateFrom: String,
        @Query("start_date_to") startDateTo: String
    ): Response<List<LessonPlanDto>>

    // 4d. Save Lesson Plan (Create)
    @POST("api/schools/lesson-plans")
    suspend fun createLessonPlan(
        @Body request: SaveLessonPlanRequest
    ): Response<SaveLessonPlanResponse>

    // 4e. Update Lesson Plan
    @PUT("api/schools/lesson-plans/{id}")
    suspend fun updateLessonPlan(
        @Path("id") id: Int,
        @Body request: SaveLessonPlanRequest
    ): Response<SaveLessonPlanResponse>

    // 4f. Journal Adjacent Lesson Navigation
    @GET("api/schools/classes/{id}/journal-adjacent")
    suspend fun getJournalAdjacentLesson(
        @Path("id") classId: Int,
        @Query("subject_id") subjectId: Int,
        @Query("lesson_number") lessonNumber: Int,
        @Query("date") date: String,
        @Query("direction") direction: String
    ): Response<JournalAdjacentResponse>

    // 5. Students by Class
    @GET("api/schools/users")
    suspend fun getStudents(
        @Query("role") role: String = "STUDENT",
        @Query("class_id") classId: Int,
        @Query("date") date: String? = null
    ): Response<List<StudentItem>>

    // 6. Subjects List
    @GET("api/schools/subjects")
    suspend fun getSubjects(): Response<List<SubjectItem>>

    // 7. Grading Systems
    @GET("api/schools/grading-systems")
    suspend fun getGradingSystems(): Response<List<GradingSystemItem>>

    @GET("api/schools/grading-systems/active")
    suspend fun getActiveGradingSystem(): Response<GradingSystemItem>

    // 8. Grades List
    @GET("api/schools/grades")
    suspend fun getGrades(
        @Query("class_id") classId: Int? = null,
        @Query("subject_id") subjectId: Int? = null
    ): Response<List<GradeResponse>>

    // 9. Batch Create / Upsert Grades
    @POST("api/schools/grades/batch")
    suspend fun batchCreateGrades(
        @Body request: BatchCreateGradesRequest
    ): Response<List<GradeResponse>>

    // 10. Single Grade Create
    @POST("api/schools/grades")
    suspend fun createGrade(
        @Body request: CreateGradeRequest
    ): Response<GradeResponse>

    // 10b. Single Grade Update
    @PUT("api/schools/grades/{id}")
    suspend fun updateGrade(
        @Path("id") gradeId: Int,
        @Body request: CreateGradeRequest
    ): Response<GradeResponse>

    // 10c. Single Grade Delete
    @DELETE("api/schools/grades/{id}")
    suspend fun deleteGrade(
        @Path("id") gradeId: Int
    ): Response<Map<String, Any>>

    // 11. Change Grade Status (Approve / Lock)
    @POST("api/schools/grades/change-status")
    suspend fun changeGradeStatus(
        @Body request: ChangeGradeStatusRequest
    ): Response<Map<String, Any>>

    // 12. Grade Comments
    @GET("api/schools/grades/{id}/comments")
    suspend fun getGradeComments(
        @Path("id") gradeId: Int
    ): Response<List<GradeCommentItem>>

    @POST("api/schools/grades/{id}/comments")
    suspend fun createGradeComment(
        @Path("id") gradeId: Int,
        @Body request: CreateCommentRequest
    ): Response<GradeCommentItem>
}
