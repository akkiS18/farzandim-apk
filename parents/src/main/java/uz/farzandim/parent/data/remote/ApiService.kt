package uz.farzandim.parent.data.remote

import retrofit2.Response
import retrofit2.http.*
import uz.farzandim.parent.data.model.*

interface ApiService {

    // 1. Schools List (Central DB Public Discovery)
    @GET("api/public/schools")
    suspend fun getSchools(): Response<List<SchoolDto>>

    // 2. School Login — tenant resolved by backend via Origin header, NOT X-School-ID
    @POST("api/schools/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // 3. Refresh Token
    @POST("api/schools/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    // 4. Ping Tenant
    @GET("api/schools/ping")
    suspend fun ping(): Response<Map<String, Any>>

    // 5. Children of Parent
    @GET("api/schools/users")
    suspend fun getStudents(
        @Query("role") role: String = "STUDENT"
    ): Response<List<StudentChildDto>>

    // 6. Child Grades
    @GET("api/schools/grades")
    suspend fun getGrades(
        @Query("student_id") studentId: Int
    ): Response<List<GradeItemDto>>

    // 7. Parent Approve Single Grade ("Ko'rdim ✓")
    @POST("api/schools/grades/{id}/parent-approve")
    suspend fun parentApproveGrade(
        @Path("id") gradeId: Int
    ): Response<Map<String, Any>>

    // 8. Class Schedule
    @GET("api/schools/classes/{id}/schedule")
    suspend fun getClassSchedule(
        @Path("id") classId: Int,
        @Query("date") date: String? = null
    ): Response<List<ClassScheduleItemDto>>

    // 9. Holidays
    @GET("api/schools/holidays")
    suspend fun getHolidays(): Response<List<HolidayDto>>

    // 10. Announcements
    @GET("api/schools/announcements")
    suspend fun getAnnouncements(): Response<List<AnnouncementDto>>

    // 11. Vote Poll
    @POST("api/schools/announcements/{id}/vote")
    suspend fun votePoll(
        @Path("id") announcementId: Int,
        @Body request: VotePollRequest
    ): Response<Map<String, Any>>

    // 12. Dining Menu
    @GET("api/schools/menu")
    suspend fun getMenu(
        @Query("date") date: String
    ): Response<DailyMenuResponse>

    // 13. Menu Comments
    @POST("api/schools/menu/comments")
    suspend fun createMenuComment(
        @Body request: CreateMenuCommentRequest
    ): Response<CommentItemDto>

    // 14. Grade Comments
    @GET("api/schools/grades/{id}/comments")
    suspend fun getGradeComments(
        @Path("id") gradeId: Int
    ): Response<List<CommentItemDto>>

    @POST("api/schools/grades/{id}/comments")
    suspend fun createGradeComment(
        @Path("id") gradeId: Int,
        @Body request: CreateGradeCommentRequest
    ): Response<CommentItemDto>

    // 15. Comments Feed
    @GET("api/schools/comments/feed")
    suspend fun getCommentsFeed(): Response<List<CommentItemDto>>

    // 16. Clubs
    @GET("api/schools/clubs")
    suspend fun getClubs(
        @Query("student_id") studentId: Int
    ): Response<List<ClubDto>>

    @POST("api/schools/clubs/{id}/request")
    suspend fun requestJoinClub(
        @Path("id") clubId: Int,
        @Body body: Map<String, Int>
    ): Response<Map<String, Any>>

    @POST("api/schools/clubs/{id}/cancel-request")
    suspend fun cancelClubRequest(
        @Path("id") clubId: Int,
        @Body body: Map<String, Int>
    ): Response<Map<String, Any>>

    // 17. Books
    @GET("api/schools/books")
    suspend fun getBooks(
        @Query("level") level: Int? = null
    ): Response<List<BookDto>>

    // 18. Balance & Next Charge
    @GET("api/schools/students/{id}/next-charge")
    suspend fun getNextCharge(
        @Path("id") studentId: Int
    ): Response<NextChargeDto>

    @GET("api/schools/students/{id}/balance/history")
    suspend fun getBalanceHistory(
        @Path("id") studentId: Int
    ): Response<List<TransactionDto>>

    // 19. AI Reports
    @GET("api/schools/parent/ai-reports/latest")
    suspend fun getLatestAiReport(): Response<AiReportDto>

    // 20. Update Profiles
    @PUT("api/schools/students/{id}")
    suspend fun updateStudentProfile(
        @Path("id") studentId: Int,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @PUT("api/schools/parents/{id}")
    suspend fun updateParentProfile(
        @Path("id") parentId: Int,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>
}
