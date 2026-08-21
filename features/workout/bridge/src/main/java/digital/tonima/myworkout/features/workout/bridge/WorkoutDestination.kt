package digital.tonima.myworkout.features.workout.bridge

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface WorkoutDestination : NavKey {
    @Serializable
    data object WorkoutList : WorkoutDestination

    @Serializable
    data class WorkoutEdit(val workoutId: Long? = null) : WorkoutDestination

    @Serializable
    data class WorkoutTracking(val workoutId: Long) : WorkoutDestination
}
