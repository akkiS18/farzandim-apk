package uz.farzandim.parent.ui.screens.balance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
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
import uz.farzandim.parent.data.model.TransactionDto
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.ParentViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun BalanceScreen(
    viewModel: ParentViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedChild by viewModel.selectedChild.collectAsState()
    val nextCharge by viewModel.nextCharge.collectAsState()
    val transactions by viewModel.balanceHistory.collectAsState()

    LaunchedEffect(selectedChild?.id) {
        selectedChild?.id?.let { viewModel.fetchBalanceHistory(it) }
    }

    val currencyFmt = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }
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
                text = stringResource(id = R.string.balance_title),
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Balance Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SkyBluePrimary,
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.current_balance),
                            style = Typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val bal = selectedChild?.balance ?: 0.0
                        Text(
                            text = "${currencyFmt.format(bal)} so'm",
                            style = Typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        selectedChild?.let {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${it.firstName} ${it.lastName} (${it.className ?: ""})",
                                style = Typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Next Charge Card
            if (nextCharge != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SunsetOrangeContainer,
                        border = BorderStroke(1.dp, SunsetOrangeLight.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SunsetOrangeAccent,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = stringResource(id = R.string.next_charge_label),
                                        style = Typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    nextCharge?.chargeDate?.let { date ->
                                        Text(
                                            text = date,
                                            style = Typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            nextCharge?.amount?.let { amount ->
                                Text(
                                    text = "${currencyFmt.format(amount)} so'm",
                                    style = Typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SunsetOrangeDark
                                )
                            }
                        }
                    }
                }
            }

            // Transaction History Header
            item {
                Text(
                    text = stringResource(id = R.string.payment_history),
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Transactions list or empty state
            if (transactions.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardBackgroundSecondary,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "To'lovlar tarixi mavjud emas",
                                style = Typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(transactions) { tx ->
                    TransactionItemRow(tx = tx, currencyFmt = currencyFmt)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    tx: TransactionDto,
    currencyFmt: NumberFormat
) {
    val isDeposit = tx.type == "DEPOSIT"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isDeposit) GradeMintBg else GradeCoralBg,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isDeposit) GradeMintText else GradeCoralText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = tx.description ?: if (isDeposit) "Balans to'ldirish" else "Oylik to'lov",
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = tx.createdAt?.take(10).orEmpty(),
                        style = Typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = "${if (isDeposit) "+" else "-"}${currencyFmt.format(tx.amount)} so'm",
                style = Typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDeposit) GradeMintText else GradeCoralText
            )
        }
    }
}
