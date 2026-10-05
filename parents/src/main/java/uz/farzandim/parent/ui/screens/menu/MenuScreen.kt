package uz.farzandim.parent.ui.screens.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Restaurant
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
import uz.farzandim.parent.data.model.MealSectionDto
import uz.farzandim.parent.data.model.MenuDishDto
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.ParentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: ParentViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyMenu by viewModel.dailyMenu.collectAsState()
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var isFeedbackSheetOpen by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchDailyMenu(todayDateStr)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.menu_title),
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                TextButton(
                    onClick = { isFeedbackSheetOpen = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = SunsetOrangeAccent)
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Fikr bildirish",
                        style = Typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Menu Content
            val meals = dailyMenu?.meals ?: emptyList()

            if (meals.isEmpty()) {
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
                            color = SunsetOrangeContainer,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = SunsetOrangeAccent,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Text(
                            text = "Bugungi taomnoma hali kiritilmagan",
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(meals) { mealSection ->
                        MealSectionCard(section = mealSection)
                    }

                    dailyMenu?.notes?.let { notes ->
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = CardBackgroundSecondary,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Eslatma: $notes",
                                    style = Typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }

        // Feedback Bottom Sheet
        if (isFeedbackSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = {
                    isFeedbackSheetOpen = false
                    feedbackText = ""
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
                        text = stringResource(id = R.string.leave_menu_comment),
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        placeholder = {
                            Text(
                                text = "Taom sifati, ta'mi yoki takliflaringizni yozing...",
                                style = Typography.bodyMedium,
                                color = TextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SunsetOrangeAccent,
                            unfocusedBorderColor = BorderLight
                        )
                    )

                    Button(
                        onClick = {
                            if (feedbackText.isNotBlank() && !isSending) {
                                isSending = true
                                viewModel.createMenuComment(todayDateStr, feedbackText) {
                                    isSending = false
                                    isFeedbackSheetOpen = false
                                    feedbackText = ""
                                }
                            }
                        },
                        enabled = feedbackText.isNotBlank() && !isSending,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SunsetOrangeAccent,
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
fun MealSectionCard(section: MealSectionDto) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Section Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SunsetOrangeContainer.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SunsetOrangeAccent, CircleShape)
                )
                Text(
                    text = section.intervalName,
                    style = Typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SunsetOrangeDark,
                    letterSpacing = 0.5.sp
                )
            }

            HorizontalDivider(color = CardBorder, thickness = 1.dp)

            // Dish items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                section.dishes.forEach { dish ->
                    DishRow(dish = dish)
                }
            }
        }
    }
}

@Composable
fun DishRow(dish: MenuDishDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dish.name,
                style = Typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            dish.composition?.let { comp ->
                Text(
                    text = comp,
                    style = Typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dish.calories?.let { cal ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CardBackgroundSecondary
                ) {
                    Text(
                        text = "$cal kkal",
                        style = Typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            dish.weight?.let { w ->
                Text(
                    text = w,
                    style = Typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
