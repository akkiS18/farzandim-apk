package uz.farzandim.parent.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import uz.farzandim.parent.BuildConfig
import uz.farzandim.parent.R
import uz.farzandim.parent.data.local.entity.AnnouncementEntity
import uz.farzandim.parent.data.local.entity.ParentGradeEntity
import uz.farzandim.parent.data.model.PollOptionDto
import uz.farzandim.parent.ui.theme.*

@Composable
fun ActivityFeedStream(
    grades: List<ParentGradeEntity>,
    announcements: List<AnnouncementEntity>,
    onApproveGrade: (Int) -> Unit,
    onVotePoll: (announcementId: Int, optionId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.feed_title),
                style = Typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        if (grades.isEmpty() && announcements.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SoftSlateCard,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.no_feed_items),
                        style = Typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            // Display latest grades first (take up to 5)
            grades.take(5).forEach { grade ->
                GradeFeedCard(
                    grade = grade,
                    onApprove = { onApproveGrade(grade.id) }
                )
            }

            // Display announcements
            announcements.take(3).forEach { announcement ->
                AnnouncementFeedCard(
                    announcement = announcement,
                    onVote = { optId -> onVotePoll(announcement.id, optId) }
                )
            }
        }
    }
}

@Composable
fun GradeFeedCard(
    grade: ParentGradeEntity,
    onApprove: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic badge coloring
    val (badgeBg, badgeText) = when (grade.gradeType) {
        "ATTENDANCE" -> when (grade.value) {
            "+" -> Pair(GradeMintBg, GradeMintText)
            "-" -> Pair(GradeCoralBg, GradeCoralText)
            "k" -> Pair(GradeAmberBg, GradeAmberText)
            else -> Pair(SkyBlueContainer, SkyBluePrimary)
        }
        "BEHAVIOR" -> Pair(BehaviorVioletBg, BehaviorVioletText)
        else -> when (grade.value) {
            "5" -> Pair(GradeMintBg, GradeMintText)
            "4" -> Pair(SkyBlueContainer, SkyBluePrimary)
            "3" -> Pair(GradeAmberBg, GradeAmberText)
            "2" -> Pair(GradeCoralBg, GradeCoralText)
            else -> Pair(SoftSlateCard, TextPrimary)
        }
    }

    val displayValue = when (grade.gradeType) {
        "BEHAVIOR" -> "★ ${grade.value}"
        else -> grade.value
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Badge icon
            Surface(
                shape = CircleShape,
                color = badgeBg,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = displayValue,
                        color = badgeText,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = grade.subjectName.ifBlank { "Fan" },
                    style = Typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    grade.teacherName?.let { teacher ->
                        Text(
                            text = teacher,
                            style = Typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = "•", style = Typography.bodySmall, color = TextTertiary)
                    }
                    Text(
                        text = grade.gradeDate.ifBlank { "" },
                        style = Typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }

            // Action button or approved status
            if (!grade.approvedByParent) {
                Button(
                    onClick = onApprove,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SunsetOrangeAccent,
                        contentColor = CardBackground
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.approve_grade_btn),
                        style = Typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GradeMintBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = GradeMintText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.approved_badge),
                            style = Typography.labelSmall,
                            color = GradeMintText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementFeedCard(
    announcement: AnnouncementEntity,
    onVote: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pollOptions: List<PollOptionDto> = remember(announcement.pollOptionsJson) {
        if (!announcement.pollOptionsJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<PollOptionDto>>() {}.type
                Gson().fromJson(announcement.pollOptionsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()
    }

    val imageUrls: List<String> = remember(announcement.imageUrlsJson) {
        if (!announcement.imageUrlsJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                Gson().fromJson(announcement.imageUrlsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()
    }

    var selectedFullImageUrl by remember { mutableStateOf<String?>(null) }

    val resolveImageUrl = { path: String ->
        if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            val base = BuildConfig.BASE_URL.removeSuffix("/")
            val cleanPath = if (path.startsWith("/")) path else "/$path"
            "$base$cleanPath"
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BehaviorVioletBg,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (announcement.isPoll) Icons.Default.Poll else Icons.Default.Campaign,
                                contentDescription = null,
                                tint = BehaviorVioletText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = announcement.authorName ?: "Maktab ma'muriyati",
                        style = Typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = announcement.createdAt?.take(10) ?: "",
                    style = Typography.labelSmall,
                    color = TextTertiary
                )
            }

            // Title and Content
            Text(
                text = announcement.title.orEmpty(),
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = announcement.content.orEmpty(),
                style = Typography.bodyMedium,
                color = TextSecondary,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            // Image Gallery (single image or horizontal scroll for multiple)
            if (imageUrls.isNotEmpty()) {
                if (imageUrls.size == 1) {
                    val fullUrl = resolveImageUrl(imageUrls[0])
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = "E'lon rasmi",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedFullImageUrl = fullUrl }
                    )
                } else {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(imageUrls.size) { idx ->
                            val fullUrl = resolveImageUrl(imageUrls[idx])
                            AsyncImage(
                                model = fullUrl,
                                contentDescription = "E'lon rasmi ${idx + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedFullImageUrl = fullUrl }
                            )
                        }
                    }
                }
            }

            // Poll Section if applicable
            if (announcement.isPoll && pollOptions.isNotEmpty()) {
                val hasVoted = announcement.userVotedOptionId != null
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pollOptions.forEach { opt ->
                        val isSelected = opt.id == announcement.userVotedOptionId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SkyBlueContainer else SoftSlateCard,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) SkyBluePrimary else CardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !hasVoted) {
                                    onVote(opt.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = opt.text,
                                    style = Typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SkyBluePrimary else TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (hasVoted) {
                                    val percent = opt.percentage ?: 0.0
                                    Text(
                                        text = "${percent.toInt()}%",
                                        style = Typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SkyBluePrimary else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Image Lightbox
    if (selectedFullImageUrl != null) {
        Dialog(
            onDismissRequest = { selectedFullImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .clickable { selectedFullImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = selectedFullImageUrl,
                    contentDescription = "To'liq rasm",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                IconButton(
                    onClick = { selectedFullImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Yopish",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
