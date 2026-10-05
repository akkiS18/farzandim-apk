package uz.farzandim.teacher.ui.screens.comments

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import uz.farzandim.teacher.ui.theme.*
import uz.farzandim.teacher.ui.viewmodel.CommentViewModel

private val QUICK_TEMPLATES = listOf(
    "Darsda juda faol qatnashdi 🌟",
    "Mavzuni a'lo o'zlashtirdi 👍",
    "Uy vazifasini bajarmagan ⚠️",
    "Qo'shimcha dars kerak 📚",
    "Namunali xulq va tartib ⭐",
    "Darsga kechikib keldi ⏰"
)

private val GRADE_TAGS = listOf("Baho", "Davomat", "Xulq")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(
    gradeId: String,
    studentName: String,
    className: String,
    subjectName: String,
    date: String,
    viewModel: CommentViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val comments by viewModel.comments.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    var selectedTag by remember { mutableStateOf("Baho") }

    LaunchedEffect(gradeId) {
        viewModel.setLocalGradeId(gradeId)
    }

    LaunchedEffect(Unit) {
        viewModel.eventMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Initials for avatar
    val initials = remember(studentName) {
        val parts = studentName.trim().split(" ").filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercase() ?: ""}${parts[1].firstOrNull()?.uppercase() ?: ""}"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "O'"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Baho bo'yicha izoh",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "$studentName • $className",
                            fontSize = 12.sp,
                            color = HarvardCrimsonLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Orqaga", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OxfordSlate)
            )
        },
        bottomBar = {
            Surface(
                color = CardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Quick chips scrollable row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QUICK_TEMPLATES.forEach { template ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = OxfordSlateLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                modifier = Modifier.clickable {
                                    val current = commentText.trim()
                                    if (current.isEmpty()) {
                                        viewModel.onCommentTextChange(template)
                                    } else {
                                        viewModel.onCommentTextChange("$current. $template")
                                    }
                                }
                            ) {
                                Text(
                                    text = template,
                                    fontSize = 11.sp,
                                    color = OxfordSlate,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Input & Send Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { viewModel.onCommentTextChange(it) },
                            placeholder = { Text("Izoh matnini yozing...", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 3,
                            trailingIcon = {
                                if (commentText.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onCommentTextChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Tozalash",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        )

                        IconButton(
                            onClick = { viewModel.submitComment() },
                            enabled = commentText.isNotBlank() && !isSubmitting,
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (commentText.isNotBlank()) HarvardCrimson else BorderMedium,
                                    RoundedCornerShape(22.dp)
                                )
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Yuborish", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(innerPadding)
        ) {
            // Student Context Card
            Surface(
                color = CardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OxfordSlate),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = studentName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = OxfordSlate
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$className sinfi",
                                    fontSize = 12.sp,
                                    color = HarvardCrimson,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(text = "•", fontSize = 12.sp, color = TextMuted)
                                Text(
                                    text = subjectName,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Surface(
                            color = GrayBadgeBg,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                        ) {
                            Text(
                                text = date,
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Category Filter Pills (Baho, Davomat, Xulq)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GRADE_TAGS.forEach { tag ->
                            val isSelected = selectedTag == tag
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) HarvardCrimson else CardBackground,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) HarvardCrimson else BorderLight
                                ),
                                modifier = Modifier.clickable { selectedTag = tag }
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Comments Stream
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = BorderMedium,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Ushbu baho uchun hali izoh yozilmagan",
                            color = OxfordSlate,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Pastdagi tezkor tugmalar yoki matn maydoni orqali ota-onaga izoh qoldirishingiz mumkin",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(comments) { comment ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = comment.authorName ?: "O'qituvchi",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = OxfordSlate
                                        )
                                        Surface(
                                            color = OxfordSlateLight,
                                            shape = RoundedCornerShape(3.dp)
                                        ) {
                                            Text(
                                                text = selectedTag,
                                                fontSize = 10.sp,
                                                color = OxfordSlate,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (comment.syncStatus == "SYNCED") {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Sinxronlangan",
                                                tint = EmeraldBadge,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = "Kutilmoqda (Oflayn)",
                                                tint = AmberBadge,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Text(
                                            text = comment.createdAt.take(16).replace("T", " "),
                                            fontSize = 10.sp,
                                            color = TextMuted,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Text(
                                    text = comment.content,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
