package uz.farzandim.teacher.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = HarvardCrimson,
    onPrimary = CardBackground,
    primaryContainer = HarvardCrimsonLight,
    onPrimaryContainer = HarvardCrimsonDark,
    secondary = OxfordSlate,
    onSecondary = CardBackground,
    secondaryContainer = OxfordSlateLight,
    onSecondaryContainer = OxfordSlateDark,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    outline = BorderLight,
    outlineVariant = BorderMedium
)

@Composable
fun FarzandimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Keep clean light academic theme matching web
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = OxfordSlate.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
