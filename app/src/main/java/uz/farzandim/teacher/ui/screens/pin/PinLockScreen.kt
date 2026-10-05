package uz.farzandim.teacher.ui.screens.pin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.farzandim.teacher.data.local.SessionManager
import uz.farzandim.teacher.ui.components.PinDots
import uz.farzandim.teacher.ui.components.PinKeypad
import uz.farzandim.teacher.ui.theme.*
import uz.farzandim.teacher.ui.viewmodel.AuthViewModel
import uz.farzandim.teacher.util.BiometricHelper

@Composable
fun PinLockScreen(
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    onUnlockSuccess: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userFullName by authViewModel.userFullName.collectAsState()

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isBiometricSupported by remember { mutableStateOf(false) }
    var isBiometricEnabled by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Check biometric support and preferences
    LaunchedEffect(Unit) {
        val available = BiometricHelper.isBiometricAvailable(context)
        val enabled = sessionManager.isBiometricEnabled()
        isBiometricSupported = available
        isBiometricEnabled = enabled

        if (available && enabled && context is FragmentActivity) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                onSuccess = { onUnlockSuccess() },
                onError = { /* Keep on PIN screen */ }
            )
        }
    }

    fun promptBiometric() {
        if (isBiometricSupported && context is FragmentActivity) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                onSuccess = { onUnlockSuccess() },
                onError = { /* Remain on PIN */ }
            )
        }
    }

    fun handleDigit(digit: String) {
        if (enteredPin.length >= 4) return

        val newPin = enteredPin + digit
        enteredPin = newPin
        isError = false
        errorMessage = null

        if (newPin.length == 4) {
            coroutineScope.launch {
                val isValid = sessionManager.verifyPin(newPin)
                if (isValid) {
                    onUnlockSuccess()
                } else {
                    isError = true
                    errorMessage = "PIN kod noto'g'ri. Qaytadan urinib ko'ring!"
                    delay(300)
                    enteredPin = ""
                }
            }
        }
    }

    fun handleBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
            errorMessage = null
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Avatar & Greeting & Pin Dots
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 28.dp)
            ) {
                // Profile Avatar Icon
                Surface(
                    shape = CircleShape,
                    color = OxfordSlateLight,
                    modifier = Modifier.size(64.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = HarvardCrimson,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userFullName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OxfordSlate,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Ilovaga kirish uchun PIN kodni tering",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4 PIN Dots
                PinDots(
                    pinLength = enteredPin.length,
                    isError = isError
                )

                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = RoseBadge,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Keypad & Bottom Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                PinKeypad(
                    onDigitClick = { handleDigit(it) },
                    onBackspaceClick = { handleBackspace() },
                    showBiometric = isBiometricSupported && isBiometricEnabled,
                    onBiometricClick = { promptBiometric() }
                )

                // Switch user / Logout button
                Row(
                    modifier = Modifier
                        .clickable { showLogoutDialog = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Chiqish",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Boshqa hisob bilan kirish",
                        fontSize = 13.sp,
                        color = TextSecondary,
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
                    text = "Hisobdan chiqish",
                    fontWeight = FontWeight.Bold,
                    color = OxfordSlate
                )
            },
            text = {
                Text(
                    text = "Haqiqatan ham tizimdan chiqmoqchimisiz? Qayta kirish uchun telefon raqami va parolni kiritish kerak bo'ladi.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout(onLogout)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseBadge),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ha, chiqish", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Bekor qilish", color = TextSecondary)
                }
            },
            containerColor = CardBackground
        )
    }
}
