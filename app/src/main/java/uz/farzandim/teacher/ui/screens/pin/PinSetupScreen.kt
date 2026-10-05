package uz.farzandim.teacher.ui.screens.pin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uz.farzandim.teacher.data.local.SessionManager
import uz.farzandim.teacher.ui.components.PinDots
import uz.farzandim.teacher.ui.components.PinKeypad
import uz.farzandim.teacher.ui.theme.*
import uz.farzandim.teacher.util.BiometricHelper

@Composable
fun PinSetupScreen(
    sessionManager: SessionManager,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(1) } // 1: Enter first, 2: Confirm
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBiometricDialog by remember { mutableStateOf(false) }

    val currentPin = if (step == 1) firstPin else confirmPin

    fun handleDigit(digit: String) {
        if (currentPin.length >= 4) return

        if (step == 1) {
            val newPin = firstPin + digit
            firstPin = newPin
            if (newPin.length == 4) {
                step = 2
            }
        } else {
            val newConfirm = confirmPin + digit
            confirmPin = newConfirm
            if (newConfirm.length == 4) {
                if (newConfirm == firstPin) {
                    coroutineScope.launch {
                        sessionManager.savePin(newConfirm)
                        if (BiometricHelper.isBiometricAvailable(context)) {
                            showBiometricDialog = true
                        } else {
                            onSetupComplete()
                        }
                    }
                } else {
                    isError = true
                    errorMessage = "PIN kodlar mos kelmadi. Qaytadan kiriting!"
                    firstPin = ""
                    confirmPin = ""
                    step = 1
                }
            }
        }
    }

    fun handleBackspace() {
        if (step == 1) {
            if (firstPin.isNotEmpty()) {
                firstPin = firstPin.dropLast(1)
                isError = false
                errorMessage = null
            }
        } else {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            } else {
                step = 1
            }
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
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = OxfordSlateLight,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = HarvardCrimson,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = if (step == 1) "PIN kod o'rnating" else "PIN kodni tasdiqlang",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OxfordSlate
                )

                Text(
                    text = if (step == 1)
                        "Ilovaga tezkor va xavfsiz kirish uchun 4 xonali shaxsiy kod kiriting"
                    else
                        "Tasdiqlash uchun xuddi shu kodni qayta kiriting",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4 Indicator Dots
                PinDots(
                    pinLength = currentPin.length,
                    isError = isError
                )

                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = RoseBadge,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Bank-style Number Keypad
            PinKeypad(
                onDigitClick = { handleDigit(it) },
                onBackspaceClick = { handleBackspace() },
                showBiometric = false,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }

    // Biometric Opt-In Dialog
    if (showBiometricDialog) {
        AlertDialog(
            onDismissRequest = {
                coroutineScope.launch {
                    sessionManager.setBiometricEnabled(false)
                    onSetupComplete()
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = HarvardCrimson,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Biometrik kirish",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OxfordSlate
                )
            },
            text = {
                Text(
                    text = "Keyingi safar ilovaga tezroq kirish uchun barmoq izi yoki Face ID dan foydalanishni yoqasizmi?",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            sessionManager.setBiometricEnabled(true)
                            showBiometricDialog = false
                            onSetupComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvardCrimson),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ha, yoqilsin", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            sessionManager.setBiometricEnabled(false)
                            showBiometricDialog = false
                            onSetupComplete()
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Keyinroq", color = TextSecondary)
                }
            },
            containerColor = CardBackground
        )
    }
}
