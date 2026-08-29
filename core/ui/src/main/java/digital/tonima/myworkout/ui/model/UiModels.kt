package digital.tonima.myworkout.ui.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class GamificationStatsUiModel(
    val totalXp: Int,
    val currentLevel: Int,
    val currentStreak: Int,
    val lastWorkoutTimestamp: Long,
)

@Immutable
data class MasterExerciseUiModel(
    val id: Long,
    val name: String,
    val description: String,
)

@Immutable
data class WorkoutUiModel(
    val id: Long,
    val name: String,
    val description: String,
    val exercises: ImmutableList<ExerciseUiModel>,
)

@Immutable
data class ExerciseUiModel(
    val id: Long,
    val masterExerciseId: Long,
    val name: String,
    val order: Int,
    val sets: ImmutableList<SetUiModel>,
)

@Immutable
data class SetUiModel(
    val id: Long,
    val targetWeight: Double,
    val targetReps: Int,
    val restInterval: Int,
    val notes: String,
    val order: Int,
)

@Immutable
data class SessionUiModel(
    val id: Long,
    val workoutId: Long?,
    val workoutName: String?,
    val startTime: Long,
    val endTime: Long?,
    val totalVolume: Double,
    val xpGained: Int,
    val logs: ImmutableList<LogUiModel>,
)

@Immutable
data class LogUiModel(
    val id: Long,
    val sessionId: Long,
    val exerciseId: Long,
    val setId: Long,
    val actualWeight: Double,
    val actualReps: Int,
    val timestamp: Long,
)

@Immutable
data class AchievementUiModel(
    val id: Long,
    val type: String,
    val name: String,
    val description: String,
    val timestamp: Long,
    val level: Int,
)
