package uz.farzandim.parent.ui.screens.comments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.data.model.CommentItemDto
import uz.farzandim.parent.ui.components.GradeBadge
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.ParentViewModel

enum class CommentFilter(val label: String) {
    ALL("Barchasi"),
    GRADES("Baholar"),
    MENU("Taomnoma")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    viewModel: ParentViewModel,
    modifier: Modifier = Modifier
) {
    val comments by viewModel.comments.collectAsState()
    var selectedFilter by remember { mutableStateOf(CommentFilter.ALL) }
    var selectedCommentForReply by remember { mutableStateOf<CommentItemDto?>(null) }
    var replyText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val filteredComments = remember(comments, selectedFilter) {
        when (selectedFilter) {
            CommentFilter.ALL -> comments
            CommentFilter.GRADES -> comments.filter { it.type == "GRADE" || it.gradeId != null }
            CommentFilter.MENU -> comments.filter { it.type == "MENU" || it.gradeId == null }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchComments()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.comments_title),
                    style = Typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CommentFilter.values()) { filter ->
                        val isSelected = filter == selectedFilter
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) SkyBluePrimary else CardBackgroundSecondary,
                            border = if (isSelected) null else BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = filter.label,
                                style = Typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Comments List
            if (filteredComments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SkyBlueContainer,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = null,
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(id = R.string.no_messages),
                            style = Typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredComments) { item ->
                        CommentCard(
                            item = item,
                            onClick = { selectedCommentForReply = item }
                        )
                    }
                }
            }
        }

        // Reply Bottom Sheet
        if (selectedCommentForReply != null) {
            val target = selectedCommentForReply!!
            ModalBottomSheet(
                onDismissRequest = {
                    selectedCommentForReply = null
                    replyText = ""
                },
                containerColor = CardBackground,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = target.subjectName ?: "Muloqot",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Previous message quotation
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CardBackgroundSecondary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = target.authorName,
                                style = Typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary
                            )
                            Text(
                                text = target.content,
                                style = Typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Input
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.chat_input_hint),
                                style = Typography.bodyMedium,
                                color = TextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = BorderLight
                        )
                    )

                    // Send Button
                    Button(
                        onClick = {
                            if (replyText.isNotBlank() && !isSending) {
                                isSending = true
                                if (target.gradeId != null) {
                                    viewModel.createGradeComment(target.gradeId, replyText) {
                                        isSending = false
                                        selectedCommentForReply = null
                                        replyText = ""
                                    }
                                } else {
                                    viewModel.createMenuComment(
                                        menuDate = target.createdAt?.take(10).orEmpty(),
                                        content = replyText
                                    ) {
                                        isSending = false
                                        selectedCommentForReply = null
                                        replyText = ""
                                    }
                                }
                            }
                        },
                        enabled = replyText.isNotBlank() && !isSending,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SkyBluePrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.send_btn),
                                    style = Typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentCard(
    item: CommentItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMenu = item.type == "MENU" || item.gradeId == null

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = if (isMenu) SunsetOrangeContainer else SkyBlueContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isMenu) Icons.Default.Restaurant else Icons.Default.School,
                        contentDescription = null,
                        tint = if (isMenu) SunsetOrangeAccent else SkyBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.subjectName ?: if (isMenu) "Taomnoma" else "Dars izohi",
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.createdAt?.take(10).orEmpty(),
                        style = Typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.authorName,
                    style = Typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SkyBluePrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.content,
                    style = Typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Grade badge if available
            item.gradeValue?.let { gVal ->
                GradeBadge(
                    value = gVal,
                    gradeType = "MASTERY",
                    onClick = {}
                )
            }
        }
    }
}
