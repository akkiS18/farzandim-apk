package uz.farzandim.parent.ui.screens.clubs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.data.local.entity.ClubEntity
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.ParentViewModel

@Composable
fun ClubsScreen(
    viewModel: ParentViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedChild by viewModel.selectedChild.collectAsState()
    val clubs by viewModel.clubs.collectAsState()

    LaunchedEffect(selectedChild?.id) {
        selectedChild?.id?.let { viewModel.fetchClubs(it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = stringResource(id = R.string.clubs_title),
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (clubs.isEmpty()) {
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
                        color = GradeAmberBg,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = GradeAmberText,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Text(
                        text = "To'garaklar ro'yxati topilmadi",
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(clubs) { club ->
                    ClubCard(
                        club = club,
                        onJoin = {
                            selectedChild?.id?.let { sid ->
                                viewModel.requestJoinClub(club.id, sid)
                            }
                        },
                        onCancel = {
                            selectedChild?.id?.let { sid ->
                                viewModel.cancelClubRequest(club.id, sid)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun ClubCard(
    club: ClubEntity,
    onJoin: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = club.name,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                when (club.status) {
                    "APPROVED" -> {
                        Surface(shape = RoundedCornerShape(8.dp), color = GradeMintBg) {
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
                                    text = stringResource(id = R.string.club_status_approved),
                                    style = Typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GradeMintText
                                )
                            }
                        }
                    }
                    "PENDING" -> {
                        Surface(shape = RoundedCornerShape(8.dp), color = GradeAmberBg) {
                            Text(
                                text = stringResource(id = R.string.club_status_pending),
                                style = Typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GradeAmberText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            club.description?.let { desc ->
                Text(
                    text = desc,
                    style = Typography.bodySmall,
                    color = TextSecondary
                )
            }

            club.teacherName?.let { teacher ->
                Text(
                    text = "O'qituvchi: $teacher",
                    style = Typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }

            club.scheduleText?.let { sched ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = sched,
                        style = Typography.labelSmall,
                        color = SkyBluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Action Button
            when (club.status) {
                "NONE", null -> {
                    Button(
                        onClick = onJoin,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.club_join_req),
                            style = Typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                "PENDING" -> {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GradeCoralText),
                        border = BorderStroke(1.dp, GradeCoralBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.club_cancel_req),
                            style = Typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
