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
        primary = GymRedPrimary,
        secondary = GymRedSecondary,
        tertiary = GymRedTertiary,
        background = DarkGymBackground,
        surface = DarkGymSurface,
        surfaceVariant = DarkGymCard,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White,
        outline = BorderColor,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = GymRedPrimary,
        secondary = GymRedSecondary,
        tertiary = GymRedTertiary,
        background = Color(0xFFFDFDFD),
        surface = Color.White,
        surfaceVariant = Color(0xFFF5F5F5),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color(0xFF1A1A1A),
        onSurface = Color(0xFF1A1A1A),
        outline = Color(0xFFE0E0E0),
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
