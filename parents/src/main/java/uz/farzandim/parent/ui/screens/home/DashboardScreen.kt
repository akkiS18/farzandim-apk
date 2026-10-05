package uz.farzandim.parent.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.ui.components.*
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.ParentViewModel

@Composable
fun DashboardScreen(
    viewModel: ParentViewModel,
    onOpenDiary: () -> Unit,
    onOpenDynamics: () -> Unit,
    onOpenBalance: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenAnnouncements: () -> Unit,
    onOpenClubs: () -> Unit,
    onOpenBooks: () -> Unit,
    onOpenAiReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val children by viewModel.children.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
    val selectedChild by viewModel.selectedChild.collectAsState()
    val unapprovedGradesCount by viewModel.unapprovedGradesCount.collectAsState()
    val todayGrades by viewModel.todayGrades.collectAsState()
    val allGrades by viewModel.grades.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val nextCharge by viewModel.nextCharge.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
    ) {
        // Top App Bar / Greeting Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Assalomu alaykum!",
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = selectedChild?.let { "${it.firstName} ta'lim jarayoni" }
                        ?: stringResource(id = R.string.app_subtitle),
                    style = Typography.bodySmall,
                    color = TextSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Refresh button
                IconButton(
                    onClick = { viewModel.refreshInitialData() },
                    modifier = Modifier.size(40.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = SkyBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Notification Bell with Badge
                BadgedBox(
                    badge = {
                        if (unapprovedGradesCount > 0) {
                            Badge(
                                containerColor = SunsetOrangeAccent,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (unapprovedGradesCount > 9) "9+" else unapprovedGradesCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SoftSlateCard,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onOpenDiary() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Children Carousel
        ChildCarousel(
            children = children,
            selectedChildId = selectedChildId,
            onChildSelected = { id -> viewModel.selectChild(id) },
            todayGradesCount = todayGrades.size,
            isLoading = isLoading,
            onRefresh = { viewModel.refreshInitialData() }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Pinned Action Cards (Urgent Alerts)
        PinnedActionCards(
            unapprovedGradesCount = unapprovedGradesCount,
            nextCharge = nextCharge,
            onOpenDiary = onOpenDiary,
            onOpenBalance = onOpenBalance
        )

        Spacer(modifier = Modifier.height(22.dp))

        // 3. Quick Action Grid (7 Services)
        QuickActionGrid(
            onOpenDynamics = onOpenDynamics,
            onOpenBalance = onOpenBalance,
            onOpenMenu = onOpenMenu,
            onOpenAnnouncements = onOpenAnnouncements,
            onOpenClubs = onOpenClubs,
            onOpenBooks = onOpenBooks,
            onOpenAiReport = onOpenAiReport
        )

        Spacer(modifier = Modifier.height(26.dp))

        // 4. Activity Feed Stream (Latest Grades & Announcements)
        ActivityFeedStream(
            grades = allGrades,
            announcements = announcements,
            onApproveGrade = { id -> viewModel.approveGrade(id) },
            onVotePoll = { annId, optId -> viewModel.votePoll(annId, optId) }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
