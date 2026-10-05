package uz.farzandim.parent.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import uz.farzandim.parent.R
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.AuthViewModel
import uz.farzandim.parent.ui.viewmodel.ParentViewModel
import uz.farzandim.parent.util.LocaleHelper

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinViewModel(),
    parentViewModel: ParentViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val currentLang by authViewModel.language.collectAsState()
    val userFullName by authViewModel.userFullName.collectAsState()
    val subdomain by authViewModel.subdomain.collectAsState()
    val schoolName by authViewModel.schoolName.collectAsState()
    val biometricEnabled by authViewModel.biometricEnabled.collectAsState()
    val children by parentViewModel.children.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Title
        Text(
            text = stringResource(id = R.string.settings_title),
            style = Typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // 1. Profile Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CardBackground,
            border = BorderStroke(1.dp, CardBorder),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (userFullName.isNotBlank()) userFullName else "Ota-ona",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Maktab: ${schoolName ?: subdomain}",
                        style = Typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // 2. Children Section
        if (children.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.children_section),
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            children.forEach { child ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = child.firstName.firstOrNull()?.toString() ?: "O",
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBluePrimary,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${child.firstName} ${child.lastName}",
                                style = Typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${child.className ?: ""} • INA: ${child.ina ?: child.id}",
                                style = Typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // 3. Language Selection Section
        Text(
            text = stringResource(id = R.string.language_section),
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CardBackground,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                val languages = listOf(
                    Triple("uz", "O'zbekcha", "🇺🇿"),
                    Triple("ru", "Русский", "🇷🇺"),
                    Triple("en", "English", "🇬🇧")
                )

                languages.forEachIndexed { index, (code, name, flag) ->
                    val isSelected = currentLang == code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                authViewModel.setLanguage(code)
                                LocaleHelper.setLocale(context, code)
                                (context as? Activity)?.recreate()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = flag, fontSize = 20.sp)
                            Text(
                                text = name,
                                style = Typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SkyBluePrimary else TextPrimary
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (index < languages.size - 1) {
                        HorizontalDivider(
                            color = CardBorder,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // 4. Security Section
        Text(
            text = stringResource(id = R.string.security_section),
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CardBackground,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Biometrics Switch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(id = R.string.biometric_auth),
                                style = Typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Barmoq izi orqali tezkor kirish",
                                style = Typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { authViewModel.setBiometricEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SkyBluePrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 5. Logout Button
        Button(
            onClick = { showLogoutDialog = true },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GradeCoralBg,
                contentColor = GradeCoralText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = GradeCoralText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.logout_btn),
                style = Typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = GradeCoralText
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.logout_confirm_title),
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.logout_confirm_msg),
                    style = Typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GradeCoralText)
                ) {
                    Text(text = stringResource(id = R.string.logout_btn), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel_btn))
                }
            }
        )
    }
}
