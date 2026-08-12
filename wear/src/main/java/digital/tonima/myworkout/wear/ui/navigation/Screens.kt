package digital.tonima.myworkout.wear.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object WorkoutList : Screen

    @Serializable
    data class WorkoutExecution(val workoutId: Long) : Screen
}
