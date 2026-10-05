package uz.farzandim.parent.ui.screens.pin

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.farzandim.parent.R
import uz.farzandim.parent.ui.components.PinDots
import uz.farzandim.parent.ui.components.PinKeypad
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.AuthViewModel

enum class PinScreenMode {
    ENTER,
    CREATE,
    CONFIRM
}

@Composable
fun PinScreen(
    viewModel: AuthViewModel,
    mode: PinScreenMode = PinScreenMode.ENTER,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val biometricEnabled by viewModel.biometricEnabled.collectAsState()

    var currentMode by remember { mutableStateOf(mode) }
    var enteredPin by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun showBiometricPrompt() {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometric_prompt_title))
            .setSubtitle(context.getString(R.string.biometric_prompt_desc))
            .setNegativeButtonText(context.getString(R.string.cancel_btn))
            .build()

        prompt.authenticate(promptInfo)
    }

    LaunchedEffect(Unit) {
        if (currentMode == PinScreenMode.ENTER && biometricEnabled) {
            val biometricManager = BiometricManager.from(context)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                showBiometricPrompt()
            }
        }
    }

    fun handleDigit(digit: Int) {
        if (enteredPin.length < 4) {
            enteredPin += digit
            isError = false
            errorMessage = null

            if (enteredPin.length == 4) {
                when (currentMode) {
                    PinScreenMode.ENTER -> {
                        scope.launch {
                            val isValid = viewModel.verifyPin(enteredPin)
                            if (isValid) {
                                onSuccess()
                            } else {
                                isError = true
                                errorMessage = context.getString(R.string.pin_wrong)
                                delay(600)
                                enteredPin = ""
                            }
                        }
                    }
                    PinScreenMode.CREATE -> {
                        firstPin = enteredPin
                        enteredPin = ""
                        currentMode = PinScreenMode.CONFIRM
                    }
                    PinScreenMode.CONFIRM -> {
                        if (enteredPin == firstPin) {
                            viewModel.setPin(enteredPin) {
                                onSuccess()
                            }
                        } else {
                            isError = true
                            errorMessage = context.getString(R.string.pin_mismatch)
                            scope.launch {
                                delay(600)
                                enteredPin = ""
                                currentMode = PinScreenMode.CREATE
                            }
                        }
                    }
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

    val titleText = when (currentMode) {
        PinScreenMode.ENTER -> stringResource(id = R.string.pin_enter_title)
        PinScreenMode.CREATE -> stringResource(id = R.string.pin_create_title)
        PinScreenMode.CONFIRM -> stringResource(id = R.string.pin_confirm_title)
    }

    val descText = when (currentMode) {
        PinScreenMode.CREATE -> stringResource(id = R.string.pin_create_desc)
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SkyBlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Title & Description
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = titleText,
                    style = Typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (descText != null) {
                    Text(
                        text = descText,
                        style = Typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = Typography.labelMedium,
                        color = GradeCoral,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 4 Dots
            PinDots(
                pinLength = enteredPin.length,
                isError = isError,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Keypad
            PinKeypad(
                onDigitClick = { handleDigit(it) },
                onBackspaceClick = { handleBackspace() },
                onBiometricClick = if (currentMode == PinScreenMode.ENTER && biometricEnabled) {
                    { showBiometricPrompt() }
                } else null
            )
        }
    }
}
