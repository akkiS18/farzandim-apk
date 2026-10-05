package uz.farzandim.parent

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import uz.farzandim.parent.data.local.SessionManager
import uz.farzandim.parent.ui.screens.auth.LoginScreen
import uz.farzandim.parent.ui.screens.main.MainContainerScreen
import uz.farzandim.parent.ui.screens.pin.PinScreen
import uz.farzandim.parent.ui.screens.pin.PinScreenMode
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.AuthViewModel
import uz.farzandim.parent.util.LocaleHelper

class MainActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModel()

    override fun attachBaseContext(newBase: Context) {
        val sessionManager = SessionManager(newBase)
        val savedLang = runBlocking {
            try {
                sessionManager.languageFlow.first()
            } catch (_: Exception) {
                "uz"
            }
        }
        val localizedContext = LocaleHelper.setLocale(newBase, savedLang)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentLang by authViewModel.language.collectAsState()
            val baseContext = LocalContext.current
            val baseConfig = LocalConfiguration.current

            val localizedContext = remember(currentLang, baseContext) {
                LocaleHelper.setLocale(baseContext, currentLang)
            }
            val localizedConfig = remember(currentLang, baseConfig) {
                LocaleHelper.getLocalizedConfiguration(baseConfig, currentLang)
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfig,
                LocalContext provides localizedContext
            ) {
                FarzandimParentTheme {
                    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
                    val isPinSet by authViewModel.isPinSet.collectAsState()

                    var isUnlocked by rememberSaveable { mutableStateOf(false) }

                    when (isAuthenticated) {
                        null -> {
                            // Neutral splash while DataStore session is reading
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AppBackground)
                            )
                        }
                        false -> {
                            LoginScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = {
                                    // Handled via state
                                }
                            )
                        }
                        true -> {
                            if (isPinSet == null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(AppBackground)
                                )
                            } else if (!isUnlocked) {
                                val pinMode = if (isPinSet == true) PinScreenMode.ENTER else PinScreenMode.CREATE
                                PinScreen(
                                    viewModel = authViewModel,
                                    mode = pinMode,
                                    onSuccess = {
                                        isUnlocked = true
                                    }
                                )
                            } else {
                                MainContainerScreen(
                                    onLogout = {
                                        authViewModel.logout {
                                            isUnlocked = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
