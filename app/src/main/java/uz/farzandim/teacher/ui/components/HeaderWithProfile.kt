package uz.farzandim.teacher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.teacher.R
import uz.farzandim.teacher.data.sync.SyncState
import uz.farzandim.teacher.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HeaderWithProfile(
    teacherName: String,
    schoolName: String = "Farzandim",
    syncState: SyncState = SyncState.Synced(),
    lastSyncedAt: Long? = null,
    currentBaseUrl: String = "",
    onLogoutClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDevLogDialog by remember { mutableStateOf(false) }
    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    fun onTitleClick() {
        val now = System.currentTimeMillis()
        if (now - lastClickTime < 1500) {
            clickCount++
            if (clickCount >= 3) {
                showDevLogDialog = true
                clickCount = 0
            }
        } else {
            clickCount = 1
        }
        lastClickTime = now
    }

    Surface(
        color = OxfordSlate,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Logo & School (Triple click to open Dev Logs)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.clickable { onTitleClick() }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(36.dp),
                        shadowElevation = 2.dp
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = "Farzandim Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "FARZANDIM",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = if (schoolName.isBlank() || schoolName.equals("Farzandim", ignoreCase = true)) "Maktab Portali" else schoolName,
                            color = HarvardCrimsonLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Subtle last-sync time — tiny, muted, only for attentive users
                        val syncLabel = remember(syncState, lastSyncedAt) {
                            when (syncState) {
                                is SyncState.Syncing -> "sinxronlanmoqda..."
                                is SyncState.Pending -> {
                                    val t = lastSyncedAt?.let { formatLastSyncedRelative(it) }
                                    if (!t.isNullOrBlank()) "$t (${syncState.count} kutilmoqda)"
                                    else "${syncState.count} ta kutilmoqda"
                                }
                                is SyncState.Offline -> {
                                    val t = lastSyncedAt?.let { formatLastSyncedRelative(it) }
                                    if (!t.isNullOrBlank()) "$t (oflayn)"
                                    else "oflayn"
                                }
                                is SyncState.Synced -> {
                                    val t = lastSyncedAt ?: syncState.syncedAt
                                    formatLastSyncedRelative(t)
                                }
                            }
                        }
                        if (!syncLabel.isNullOrBlank()) {
                            Text(
                                text = syncLabel,
                                color = Color.White.copy(alpha = 0.38f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.sp
                            )
                        }
                    }
                }

                // Right: Teacher Profile Avatar with Dropdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sync Indicator Icon
                    when (syncState) {
                        is SyncState.Synced -> {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Sinxronlangan",
                                tint = EmeraldBadge,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        is SyncState.Pending -> {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Kutilmoqda",
                                tint = AmberBadge,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        is SyncState.Syncing -> {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        is SyncState.Offline -> {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Oflayn",
                                tint = RoseBadge,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Teacher Name
                    Text(
                        text = teacherName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )

                    // Circular Avatar
                    Box {
                        val initials = teacherName.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            .joinToString("")
                            .ifEmpty { "O'" }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(HarvardCrimson)
                                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                .clickable { menuExpanded = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Profile Dropdown Menu
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(CardBackground)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = "Chiqish",
                                            tint = RoseBadge
                                        )
                                        Text("Chiqish", color = RoseBadge, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    showLogoutDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Sync Status subtle banner if pending or offline
            if (syncState is SyncState.Pending) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AmberBadgeBg)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🟡 ${syncState.message}",
                        color = AmberBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (syncState is SyncState.Offline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoseBadgeBg)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚠️ Oflayn rejim — internet aloqasi yo'q",
                        color = RoseBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Tizimdan chiqish",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Haqiqatan ham hisobingizdan chiqmoqchimisiz?",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvardCrimson),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text("Ha", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text("Yo'q", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(0.dp),
            containerColor = CardBackground
        )
    }

    if (showDevLogDialog) {
        DevLogDialog(
            currentBaseUrl = currentBaseUrl.ifEmpty { "https://api.akademx.uz" },
            onDismiss = { showDevLogDialog = false }
        )
    }
}

private fun formatLastSyncedRelative(syncedAt: Long): String {
    if (syncedAt <= 0L) return ""
    val now = System.currentTimeMillis()
    val diff = now - syncedAt
    return when {
        diff < 60_000L -> "sync: hozirgina"
        diff < 3_600_000L -> {
            val mins = diff / 60_000L
            "sync: $mins daq oldin"
        }
        diff < 86_400_000L -> {
            val hours = diff / 3_600_000L
            "sync: $hours soat oldin"
        }
        else -> {
            val sdf = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
            "sync: ${sdf.format(Date(syncedAt))}"
        }
    }
}
