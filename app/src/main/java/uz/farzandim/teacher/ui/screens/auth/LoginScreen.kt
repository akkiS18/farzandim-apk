package uz.farzandim.teacher.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.teacher.R
import uz.farzandim.teacher.ui.components.AdminCodeDialog
import uz.farzandim.teacher.ui.components.SchoolPickerDialog
import uz.farzandim.teacher.ui.theme.*
import uz.farzandim.teacher.ui.viewmodel.AuthUiState
import uz.farzandim.teacher.ui.viewmodel.AuthViewModel

class UzbekPhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.take(9)
        val formatted = StringBuilder()
        for (i in raw.indices) {
            formatted.append(raw[i])
            if ((i == 1 || i == 4 || i == 6) && i != raw.lastIndex) {
                formatted.append(" ")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                return when {
                    clamped <= 2 -> clamped
                    clamped <= 5 -> clamped + 1
                    clamped <= 7 -> clamped + 2
                    else -> clamped + 3
                }.coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                val orig = when {
                    clamped <= 2 -> clamped
                    clamped <= 6 -> (clamped - 1).coerceAtLeast(0)
                    clamped <= 9 -> (clamped - 2).coerceAtLeast(0)
                    else -> (clamped - 3).coerceAtLeast(0)
                }
                return orig.coerceAtMost(raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted.toString()), offsetMapping)
    }
}

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSubdomain by viewModel.subdomain.collectAsState()
    val currentSchoolName by viewModel.currentSchoolName.collectAsState()
    val availableSchools by viewModel.availableSchools.collectAsState()
    val isLoadingSchools by viewModel.isLoadingSchools.collectAsState()

    var phoneInput by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showSchoolPickerDialog by remember { mutableStateOf(false) }

    val phoneTransformation = remember { UzbekPhoneVisualTransformation() }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Settings / Subdomain icon at top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = {
                    showSchoolPickerDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Maktabni tanlash",
                    tint = TextMuted
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Logo & Branding
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    modifier = Modifier.size(72.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = "Farzandim Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Farzandim O'qituvchi",
                    style = Typography.headlineMedium,
                    color = OxfordSlate
                )
                Text(
                    text = "O'qituvchilar uchun jurnal va baholash portali",
                    style = Typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input: Phone Number
            OutlinedTextField(
                value = phoneInput,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    val local = if (digits.startsWith("998") && digits.length > 9) {
                        digits.drop(3)
                    } else if (digits.startsWith("998") && digits.length <= 3) {
                        ""
                    } else {
                        digits
                    }
                    if (local.length <= 9) {
                        phoneInput = local
                    }
                },
                label = { Text("Telefon raqami") },
                placeholder = { Text("90 123 45 67") },
                prefix = {
                    Text(
                        text = "+998 ",
                        color = OxfordSlate,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                },
                visualTransformation = phoneTransformation,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            )

            // Input: Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Parol") },
                placeholder = { Text("Parolingizni kiriting") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                }
            )

            // Error Banner
            if (uiState is AuthUiState.Error) {
                Surface(
                    color = RoseBadgeBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseBadgeBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = RoseBadge)
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = RoseBadge,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    viewModel.login(if (phoneInput.isNotBlank()) "+998$phoneInput" else "", password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HarvardCrimson),
                enabled = uiState !is AuthUiState.Loading
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = "Tizimga kirish",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // School Selector Link
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = GrayBadgeBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.clickable { showSchoolPickerDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = HarvardCrimson,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = currentSchoolName.ifBlank { "$currentSubdomain.akademx.uz" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OxfordSlate
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "O'zgartirish",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    // Searchable School Picker Dialog
    if (showSchoolPickerDialog) {
        SchoolPickerDialog(
            schools = availableSchools,
            isLoading = isLoadingSchools,
            currentSubdomain = currentSubdomain,
            onDismiss = { showSchoolPickerDialog = false },
            onSelectSchool = { school ->
                viewModel.selectSchool(school)
            },
            onRefresh = { viewModel.loadSchools() },
            onManualSubdomain = { manualSub ->
                viewModel.saveSubdomain(manualSub)
            }
        )
    }
}
