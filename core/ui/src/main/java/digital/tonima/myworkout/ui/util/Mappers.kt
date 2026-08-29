package digital.tonima.myworkout.ui.util

import digital.tonima.myworkout.data.model.AchievementEntity
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import digital.tonima.myworkout.data.preferences.GamificationStats
import digital.tonima.myworkout.ui.model.AchievementUiModel
import digital.tonima.myworkout.ui.model.ExerciseUiModel
import digital.tonima.myworkout.ui.model.GamificationStatsUiModel
import digital.tonima.myworkout.ui.model.LogUiModel
import digital.tonima.myworkout.ui.model.MasterExerciseUiModel
import digital.tonima.myworkout.ui.model.SessionUiModel
import digital.tonima.myworkout.ui.model.SetUiModel
import digital.tonima.myworkout.ui.model.WorkoutUiModel
import kotlinx.collections.immutable.toImmutableList

fun WorkoutWithExercises.toUiModel(): WorkoutUiModel {
    return WorkoutUiModel(
        id = workout.id,
        name = workout.name,
        description = workout.description,
        exercises = exercises.map { it.toUiModel() }.toImmutableList(),
    )
}

fun ExerciseWithSets.toUiModel(): ExerciseUiModel {
    return ExerciseUiModel(
        id = exercise.id,
        masterExerciseId = exercise.masterExerciseId,
        name = exercise.name,
        order = exercise.order,
        sets =
            sets.map { set ->
                SetUiModel(
                    id = set.id,
                    targetWeight = set.targetWeight,
                    targetReps = set.targetReps,
                    restInterval = set.restInterval,
                    notes = set.notes,
                    order = set.order,
                )
            }.sortedBy { it.order }.toImmutableList(),
    )
}

fun SessionWithLogs.toUiModel(): SessionUiModel {
    return SessionUiModel(
        id = session.id,
        workoutId = session.workoutId,
        workoutName = workout?.name,
        startTime = session.startTime,
        endTime = session.endTime,
        totalVolume = session.totalVolume,
        xpGained = session.xpGained,
        logs = logs.map { it.toUiModel() }.toImmutableList(),
    )
}

fun WorkoutLogEntity.toUiModel(): LogUiModel {
    return LogUiModel(
        id = id,
        sessionId = sessionId,
        exerciseId = masterExerciseId,
        setId = setId,
        actualWeight = actualWeight,
        actualReps = actualReps,
        timestamp = timestamp,
    )
}

fun MasterExerciseEntity.toUiModel(): MasterExerciseUiModel {
    return MasterExerciseUiModel(
        id = id,
        name = name,
        description = description,
    )
}

fun AchievementEntity.toUiModel(): AchievementUiModel {
    return AchievementUiModel(
        id = id,
        type = type,
        name = name,
        description = description,
        timestamp = timestamp,
        level = level,
    )
}

fun GamificationStats.toUiModel(): GamificationStatsUiModel {
    return GamificationStatsUiModel(
        totalXp = totalXp,
        currentLevel = currentLevel,
        currentStreak = currentStreak,
        lastWorkoutTimestamp = lastWorkoutTimestamp,
    )
}
