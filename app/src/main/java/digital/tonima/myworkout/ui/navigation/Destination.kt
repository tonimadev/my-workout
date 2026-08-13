package digital.tonima.myworkout.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {
    @Serializable
    data object WorkoutList : Destination

    @Serializable
    data class WorkoutEdit(val workoutId: Long? = null) : Destination

    @Serializable
    data class WorkoutTracking(val workoutId: Long) : Destination

    @Serializable
    data object History : Destination

    @Serializable
    data object Stats : Destination
}
