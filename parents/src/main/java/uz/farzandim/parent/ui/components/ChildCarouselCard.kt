package uz.farzandim.parent.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.data.local.entity.ChildEntity
import uz.farzandim.parent.ui.theme.*

@Composable
fun ChildCarousel(
    children: List<ChildEntity>,
    selectedChildId: Int?,
    onChildSelected: (Int) -> Unit,
    todayGradesCount: Int = 0,
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (children.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardBackground,
            border = BorderStroke(1.dp, CardBorder),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = SkyBlueContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = SkyBluePrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isLoading) stringResource(id = R.string.child_loading) else stringResource(id = R.string.child_not_found),
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isLoading) stringResource(id = R.string.please_wait) else stringResource(id = R.string.child_contact_school),
                        style = Typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (!isLoading) {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = stringResource(id = R.string.retry_btn),
                            tint = SkyBluePrimary
                        )
                    }
                }
            }
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(children) { child ->
                val isSelected = child.id == selectedChildId
                ChildCard(
                    child = child,
                    isSelected = isSelected,
                    todayGradesCount = if (isSelected) todayGradesCount else 0,
                    onClick = { onChildSelected(child.id) }
                )
            }
        }
    }
}

@Composable
private fun ChildCard(
    child: ChildEntity,
    isSelected: Boolean,
    todayGradesCount: Int,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) SkyBluePrimary else BorderLight
    val containerColor = if (isSelected) CardBackground else CardBackgroundSecondary

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        modifier = Modifier
            .width(260.dp)
            .height(130.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Avatar + Name + Class Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Initial Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected)
                                Brush.linearGradient(listOf(SkyBluePrimary, SkyBlueLight))
                            else
                                Brush.linearGradient(listOf(BorderMedium, BorderLight))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = child.firstName.firstOrNull()?.toString() ?: "O"
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${child.firstName} ${child.lastName}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = if (isSelected) SkyBlueContainer else CardBackground,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = child.className ?: "Sinf",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) SkyBlueDark else TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Bottom: Balance & Today Highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Balance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = GradeMintText,
                        modifier = Modifier.size(14.dp)
                    )
                    val formattedBalance = try {
                        java.text.NumberFormat.getNumberInstance(java.util.Locale.US).apply {
                            maximumFractionDigits = 0
                        }.format(child.balance).replace(',', ' ')
                    } catch (e: Exception) {
                        "${child.balance.toLong()}"
                    }
                    Text(
                        text = "$formattedBalance so'm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (child.balance < 0) GradeCoral else GradeMintText
                    )
                }

                // Today Highlights
                if (todayGradesCount > 0) {
                    Surface(
                        color = SunsetOrangeContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Bugun: $todayGradesCount baho",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunsetOrangeDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
