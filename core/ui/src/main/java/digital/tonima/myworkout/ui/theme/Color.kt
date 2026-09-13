package digital.tonima.myworkout.ui.theme

import androidx.compose.ui.graphics.Color

// Fitness Palette - Sporty Deep Orange (Aligns with Red Icon without looking like an error)
val GymPrimary = Color(0xFFFF5722) // Deep Orange
val GymSecondary = Color(0xFFFF9800) // Orange
val GymTertiary = Color(0xFFFFC107) // Amber

val DarkGymBackground = Color(0xFF0A0A0A)
val DarkGymSurface = Color(0xFF121212)
val DarkGymCard = Color(0xFF1E1E1E)

val NeonBlue = Color(0xFF00E5FF)
val SuccessGreen = Color(0xFF00E676)
val ErrorRed = Color(0xFFFF1744)

// Neutral Palette
val LightGrey = Color(0xFFF5F5F5)
val MediumGrey = Color(0xFF9E9E9E)

// Borders and secondary text - tuned for WCAG contrast against their respective surfaces.
// BorderColor targets >=3:1 (non-text) against DarkGymSurface; DarkOnSurfaceVariant targets
// >=4.5:1 (text) against DarkGymSurface. LightOutline/LightOnSurfaceVariant mirror this for light mode.
val BorderColor = Color(0xFF6B6B6B)
val DarkOnSurfaceVariant = Color(0xFF9E9E9E)
val LightOutline = Color(0xFF8A8A8A)
val LightOnSurfaceVariant = Color(0xFF616161)

// secondaryContainer/onSecondaryContainer - used by NavigationSuiteScaffold's selected-item pill.
// Overridden so it reads as warm/orange (Bold Performance identity) instead of Material's default
// cool lavender baseline, which was never themed and clashed with the rest of the app. Both pairs
// verified >=4.5:1 (text) between container and on-container.
val LightSecondaryContainer = Color(0xFFFFD9C2)
val LightOnSecondaryContainer = Color(0xFF7A2E00)
val DarkSecondaryContainer = Color(0xFF3D1A00)
val DarkOnSecondaryContainer = Color(0xFFFFCBA6)

// Warm-neutral surface container scale (replaces Material's default cool/lavender-tinted
// baseline). Used by NavigationSuiteScaffold, ListItem, and other M3 components that read
// surfaceContainer* instead of surface/background directly.
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFFAFAFA)
val LightSurfaceContainer = Color(0xFFF5F5F5)
val LightSurfaceContainerHigh = Color(0xFFEFEFEF)
val LightSurfaceContainerHighest = Color(0xFFE8E8E8)

val DarkSurfaceContainerLowest = Color(0xFF050505)
val DarkSurfaceContainerLow = Color(0xFF0F0F0F)
val DarkSurfaceContainer = Color(0xFF1A1A1A)
val DarkSurfaceContainerHigh = Color(0xFF212121)
val DarkSurfaceContainerHighest = Color(0xFF2A2A2A)
