package uz.farzandim.parent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.farzandim.parent.R
import uz.farzandim.parent.data.local.entity.ParentGradeEntity
import uz.farzandim.parent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeCommentBottomSheet(
    grade: ParentGradeEntity?,
    onDismiss: () -> Unit,
    onSendComment: (gradeId: Int, content: String) -> Unit
) {
    if (grade == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBackground,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "O'qituvchiga izoh yoki savol",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Grade Summary Row
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardBackgroundSecondary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = grade.subjectName,
                            style = Typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        grade.teacherName?.let {
                            Text(
                                text = it,
                                style = Typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = grade.gradeDate,
                            style = Typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    GradeBadge(
                        value = when (grade.gradeType) {
                            "BEHAVIOR" -> "★ ${grade.value}"
                            else -> grade.value
                        },
                        gradeType = grade.gradeType,
                        onClick = {}
                    )
                }
            }

            // Input Field
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.chat_input_hint),
                        style = Typography.bodyMedium,
                        color = TextMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBluePrimary,
                    unfocusedBorderColor = BorderLight
                )
            )

            // Submit Button
            Button(
                onClick = {
                    if (commentText.isNotBlank() && !isSending) {
                        isSending = true
                        onSendComment(grade.id, commentText)
                        onDismiss()
                    }
                },
                enabled = commentText.isNotBlank() && !isSending,
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
