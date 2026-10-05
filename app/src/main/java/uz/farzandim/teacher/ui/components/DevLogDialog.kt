package uz.farzandim.teacher.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import uz.farzandim.teacher.ui.theme.*
import uz.farzandim.teacher.util.AppLogger
import uz.farzandim.teacher.util.LogEntry

@Composable
fun DevLogDialog(
    currentBaseUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val logs by AppLogger.logs.collectAsState()
    var selectedLog by remember { mutableStateOf<LogEntry?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E293B),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🛠 Developer / Server Loglari",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = currentBaseUrl,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                val allText = logs.joinToString("\n\n") {
                                    val statusStr = when {
                                        it.statusCode != null -> it.statusCode.toString()
                                        it.isError -> "FAIL"
                                        else -> "INFO"
                                    }
                                    "[${it.timestamp}] ${it.method} ${it.url}\nStatus: $statusStr | Duration: ${it.durationMs ?: 0}ms\nError: ${it.error ?: "None"}\nBody: ${it.responseBody ?: "None"}"
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Farzandim Logs", allText))
                                Toast.makeText(context, "Loglar nusxalandi", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Nusxalash", tint = Color.White)
                        }

                        IconButton(
                            onClick = { AppLogger.clear() }
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Tozalash", tint = Color(0xFFF87171))
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Yopish", tint = Color.White)
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                // Log details expansion or list
                if (selectedLog != null) {
                    val log = selectedLog!!
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${log.method} (${log.statusCode ?: "FAIL"})",
                                color = if (log.isError) Color(0xFFF87171) else Color(0xFF34D399),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Orqaga",
                                color = Color(0xFF60A5FA),
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { selectedLog = null }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "URL: ${log.url}",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vaqt: ${log.timestamp} | ${log.durationMs ?: 0}ms",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        if (!log.error.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Xatolik: ${log.error}",
                                color = Color(0xFFF87171),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!log.responseBody.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Javob matni (Body):\n${log.responseBody}",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    if (logs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Hozircha hech qanday tarmoq so'rovi yozilmadi", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(logs, key = { it.id }) { log ->
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (log.isError) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF334155)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedLog = log }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val badgeBg = when {
                                            log.isError -> Color(0xFF7F1D1D)
                                            log.statusCode != null -> Color(0xFF064E3B)
                                            else -> Color(0xFF1E3A8A)
                                        }
                                        val badgeTextColor = when {
                                            log.isError -> Color(0xFFFCA5A5)
                                            log.statusCode != null -> Color(0xFF6EE7B7)
                                            else -> Color(0xFF93C5FD)
                                        }
                                        val badgeText = when {
                                            log.statusCode != null -> log.statusCode.toString()
                                            log.isError -> "ERR"
                                            else -> "INFO"
                                        }
                                        Surface(
                                            color = badgeBg,
                                            shape = RoundedCornerShape(3.dp)
                                        ) {
                                            Text(
                                                text = badgeText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeTextColor,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${log.method} ${log.url.substringAfter("/api/")}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = log.timestamp,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            }
                                            if (!log.error.isNullOrBlank()) {
                                                Text(
                                                    text = log.error,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFF87171),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
