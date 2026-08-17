package digital.tonima.myworkout.wear.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme

val GymPrimary = Color(0xFFFF5722)
val GymSecondary = Color(0xFFFF9800)
val GymTertiary = Color(0xFFFFC107)

val DarkGymBackground = Color(0xFF000000) // Pure black for OLED power savings
val DarkGymSurface = Color(0xFF121212)

val wearColorScheme: ColorScheme =
    ColorScheme(
        primary = GymPrimary,
        onPrimary = Color.Black,
        secondary = GymSecondary,
        onSecondary = Color.Black,
        tertiary = GymTertiary,
        onTertiary = Color.Black,
        background = DarkGymBackground,
        onBackground = Color.White,
        surfaceContainer = DarkGymSurface,
        onSurface = Color.White,
        onSurfaceVariant = Color.White,
        outline = Color.Gray,
    )
