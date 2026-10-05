package uz.farzandim.teacher.data.model

import com.google.gson.annotations.SerializedName

data class GradeCommentItem(
    @SerializedName("id") val id: Int,
    @SerializedName("grade_id") val gradeId: Int,
    @SerializedName("author_id") val authorId: Int,
    @SerializedName("author_name") val authorName: String? = null,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String
)

data class CreateCommentRequest(
    @SerializedName("content") val content: String
)
