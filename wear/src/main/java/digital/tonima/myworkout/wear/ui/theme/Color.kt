package digital.tonima.myworkout.wear.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme

val GymRedPrimary = Color(0xFFE53935)
val GymRedSecondary = Color(0xFFFF5252)
val GymRedTertiary = Color(0xFFB71C1C)

val DarkGymBackground = Color(0xFF000000) // Pure black for OLED power savings
val DarkGymSurface = Color(0xFF121212)
val DarkGymCard = Color(0xFF1E1E1E)

val wearColorScheme: ColorScheme =
    ColorScheme(
        primary = GymRedPrimary,
        onPrimary = Color.Black,
        secondary = GymRedSecondary,
        onSecondary = Color.Black,
        tertiary = GymRedTertiary,
        onTertiary = Color.White,
        background = DarkGymBackground,
        onBackground = Color.White,
        surfaceContainer = DarkGymSurface,
        onSurface = Color.White,
        onSurfaceVariant = Color.White,
        outline = Color.Gray,
    )
