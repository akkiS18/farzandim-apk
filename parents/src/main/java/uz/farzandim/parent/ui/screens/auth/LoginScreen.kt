package uz.farzandim.parent.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.data.local.SessionManager
import uz.farzandim.parent.ui.components.SchoolPickerDialog
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.AuthUiState
import uz.farzandim.parent.ui.viewmodel.AuthViewModel
import uz.farzandim.parent.util.PassportVisualTransformation

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSubdomain by viewModel.subdomain.collectAsState()
    val schoolName by viewModel.schoolName.collectAsState()
    val availableSchools by viewModel.availableSchools.collectAsState()
    val isLoadingSchools by viewModel.isLoadingSchools.collectAsState()

    var passportInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSchoolPickerDialog by remember { mutableStateOf(false) }

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
        // Top Bar: School info & Settings Dialog Trigger
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = SkyBlueContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { showSchoolPickerDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = schoolName ?: SessionManager.formatSubdomainToSchoolName(currentSubdomain),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SkyBlueDark
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = SkyBlueDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            IconButton(
                onClick = { showSchoolPickerDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.school_settings),
                    tint = TextSecondary
                )
            }
        }

        // Main Login Card Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Brand Logo with Parents App Icon
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.size(72.dp),
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(6.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Farzandim Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Title & Description
            Text(
                text = "Farzandim-edu",
                style = Typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = stringResource(id = R.string.login_desc),
                style = Typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Passport Input with Auto-Mask
            OutlinedTextField(
                value = passportInput,
                onValueChange = { input ->
                    val clean = input.filter { it.isLetterOrDigit() }.take(9).uppercase()
                    passportInput = clean
                },
                label = { Text(stringResource(id = R.string.passport_label)) },
                placeholder = { Text(stringResource(id = R.string.passport_hint)) },
                visualTransformation = PassportVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = SkyBluePrimary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBluePrimary,
                    unfocusedBorderColor = BorderLight
                )
            )

            // 2. Password Input
            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text(stringResource(id = R.string.password_label)) },
                placeholder = { Text(stringResource(id = R.string.password_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SkyBluePrimary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBluePrimary,
                    unfocusedBorderColor = BorderLight
                )
            )

            // Error Banner
            if (uiState is AuthUiState.Error) {
                Surface(
                    color = GradeCoralBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GradeCoralBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = GradeCoral
                        )
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = GradeCoralText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.loginWithPassport(passportInput, passwordInput)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                enabled = uiState !is AuthUiState.Loading
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.login_btn),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

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
            onManualSubdomain = { newSubdomain ->
                viewModel.saveSubdomain(newSubdomain)
            }
        )
    }
}
