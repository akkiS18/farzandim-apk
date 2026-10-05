package uz.farzandim.parent.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.ui.theme.*

data class QuickActionItem(
    val titleRes: Int,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTintColor: Color,
    val onClick: () -> Unit
)

@Composable
fun QuickActionGrid(
    onOpenDynamics: () -> Unit,
    onOpenBalance: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenAnnouncements: () -> Unit,
    onOpenClubs: () -> Unit,
    onOpenBooks: () -> Unit,
    onOpenAiReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        QuickActionItem(R.string.service_dynamics, Icons.AutoMirrored.Filled.TrendingUp, SkyBlueContainer, SkyBluePrimary, onOpenDynamics),
        QuickActionItem(R.string.service_balance, Icons.Default.AccountBalanceWallet, GradeMintBg, GradeMintText, onOpenBalance),
        QuickActionItem(R.string.service_menu, Icons.Default.Restaurant, SunsetOrangeContainer, SunsetOrangeAccent, onOpenMenu),
        QuickActionItem(R.string.service_announcements, Icons.Default.Campaign, BehaviorVioletBg, BehaviorVioletText, onOpenAnnouncements),
        QuickActionItem(R.string.service_clubs, Icons.Default.Stars, GradeAmberBg, GradeAmberText, onOpenClubs),
        QuickActionItem(R.string.service_books, Icons.AutoMirrored.Filled.MenuBook, Color(0xFFEEF2FF), Color(0xFF4F46E5), onOpenBooks),
        QuickActionItem(R.string.service_ai_report, Icons.Default.AutoAwesome, Color(0xFFECFEFF), Color(0xFF0891B2), onOpenAiReport)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(id = R.string.quick_actions),
            style = Typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.take(4).forEach { action ->
                QuickActionTile(action = action, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            items.drop(4).forEach { action ->
                QuickActionTile(action = action, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.weight(1f)) // placeholder to balance row
        }
    }
}

@Composable
private fun QuickActionTile(
    action: QuickActionItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { action.onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = action.iconBgColor,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = stringResource(id = action.titleRes),
                    tint = action.iconTintColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = stringResource(id = action.titleRes),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
