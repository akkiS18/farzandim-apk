package uz.farzandim.parent.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.ui.theme.*

enum class ParentBottomTab(
    val titleRes: Int,
    val icon: ImageVector
) {
    HOME(R.string.nav_home, Icons.Default.Home),
    DIARY(R.string.nav_diary, Icons.Default.AutoStories),
    COMMENTS(R.string.nav_comments, Icons.AutoMirrored.Filled.Chat),
    SETTINGS(R.string.nav_settings, Icons.Default.Settings)
}

@Composable
fun ParentBottomNavigationBar(
    currentTab: ParentBottomTab,
    onTabSelected: (ParentBottomTab) -> Unit,
    unapprovedGradesCount: Int = 0,
    unreadCommentsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp),
        color = CardBackground,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ParentBottomTab.values().forEach { tab ->
                val isSelected = currentTab == tab

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) SkyBluePrimary else TextMuted,
                    label = "iconColor"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) SkyBluePrimary else TextSecondary,
                    label = "textColor"
                )

                val badgeCount = when (tab) {
                    ParentBottomTab.DIARY -> unapprovedGradesCount
                    ParentBottomTab.COMMENTS -> unreadCommentsCount
                    else -> 0
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(Shapes.small)
                        .clickable { onTabSelected(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (badgeCount > 0) {
                                Badge(
                                    containerColor = SunsetOrangeAccent,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = stringResource(id = tab.titleRes),
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = stringResource(id = tab.titleRes),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}
