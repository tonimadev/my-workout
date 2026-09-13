package digital.tonima.myworkout.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = GymPrimary,
        secondary = GymSecondary,
        tertiary = GymTertiary,
        background = DarkGymBackground,
        surface = DarkGymSurface,
        surfaceVariant = DarkGymCard,
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        onSurfaceVariant = DarkOnSurfaceVariant,
        outline = BorderColor,
        secondaryContainer = DarkSecondaryContainer,
        onSecondaryContainer = DarkOnSecondaryContainer,
        surfaceContainerLowest = DarkSurfaceContainerLowest,
        surfaceContainerLow = DarkSurfaceContainerLow,
        surfaceContainer = DarkSurfaceContainer,
        surfaceContainerHigh = DarkSurfaceContainerHigh,
        surfaceContainerHighest = DarkSurfaceContainerHighest,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = GymPrimary,
        secondary = GymSecondary,
        tertiary = GymTertiary,
        background = Color(0xFFFDFDFD),
        surface = Color.White,
        surfaceVariant = Color(0xFFF5F5F5),
        // Black, not white: GymPrimary/GymSecondary are mid-brightness oranges where white text
        // fails WCAG AA (~2.5:1); black clears it comfortably (~5.5-6.6:1).
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color(0xFF1A1A1A),
        onSurface = Color(0xFF1A1A1A),
        onSurfaceVariant = LightOnSurfaceVariant,
        outline = LightOutline,
        secondaryContainer = LightSecondaryContainer,
        onSecondaryContainer = LightOnSecondaryContainer,
        surfaceContainerLowest = LightSurfaceContainerLowest,
        surfaceContainerLow = LightSurfaceContainerLow,
        surfaceContainer = LightSurfaceContainer,
        surfaceContainerHigh = LightSurfaceContainerHigh,
        surfaceContainerHighest = LightSurfaceContainerHighest,
    )

@Composable
fun MyWorkoutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled by default for consistent fitness brand
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
