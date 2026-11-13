package com.weeklymeal.planner.core.theme

import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalDecorativeMotion = staticCompositionLocalOf { true }

private val Scheme = lightColorScheme(
    primary = Primary, onPrimary = TextPrimary, primaryContainer = BackgroundTint, onPrimaryContainer = TextPrimary,
    secondary = Secondary, onSecondary = TextPrimary, secondaryContainer = BackgroundTint, onSecondaryContainer = TextPrimary,
    tertiary = Action, onTertiary = Surface, background = Background, onBackground = TextPrimary,
    surface = Surface, onSurface = TextPrimary, surfaceVariant = BackgroundTint, onSurfaceVariant = TextSecondary,
    outline = TextSecondary, outlineVariant = BorderSage, error = Error, onError = Surface,
    errorContainer = Background, onErrorContainer = Error, surfaceTint = Primary
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val motion = remember { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f }
    CompositionLocalProvider(LocalDecorativeMotion provides motion) {
        MaterialTheme(colorScheme = Scheme, typography = AppTypography, content = content)
    }
}
