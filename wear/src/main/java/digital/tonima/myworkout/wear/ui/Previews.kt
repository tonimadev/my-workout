package digital.tonima.myworkout.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices.LARGE_ROUND
import androidx.wear.tooling.preview.devices.WearDevices.RECT
import androidx.wear.tooling.preview.devices.WearDevices.SMALL_ROUND
import androidx.wear.tooling.preview.devices.WearDevices.SQUARE
import digital.tonima.myworkout.ui.model.ExerciseUiModel
import digital.tonima.myworkout.ui.model.SetUiModel
import digital.tonima.myworkout.ui.model.WorkoutUiModel
import kotlinx.collections.immutable.persistentListOf

@Preview(device = SMALL_ROUND)
@Preview(device = LARGE_ROUND)
@Preview(device = RECT)
@Preview(device = SQUARE)
@Composable
fun WorkoutListScreenPreview() {
    MaterialTheme {
        WorkoutListScreen(
            state =
                WorkoutState(
                    workouts =
                        persistentListOf(
                            WorkoutUiModel(
                                id = 1,
                                name = "Workout A",
                                description = "",
                                exercises =
                                    persistentListOf(
                                        ExerciseUiModel(
                                            id = 1,
                                            masterExerciseId = 1,
                                            name = "Squat",
                                            order = 1,
                                            sets =
                                                persistentListOf(
                                                    SetUiModel(
                                                        id = 1,
                                                        targetWeight = 100.0,
                                                        targetReps = 5,
                                                        restInterval = 60,
                                                        notes = "",
                                                        order = 1,
                                                    ),
                                                ),
                                        ),
                                    ),
                            ),
                        ),
                ),
            onWorkoutClick = {},
        )
    }
}

@Preview(device = SMALL_ROUND)
@Preview(device = LARGE_ROUND)
@Preview(device = RECT)
@Preview(device = SQUARE)
@Composable
fun WorkoutExecutionScreenPreview() {
    MaterialTheme {
        WorkoutExecutionScreen(
            state =
                WorkoutState(
                    currentWorkout =
                        WorkoutUiModel(
                            id = 1,
                            name = "Workout A",
                            description = "",
                            exercises =
                                persistentListOf(
                                    ExerciseUiModel(
                                        id = 1,
                                        masterExerciseId = 1,
                                        name = "Squat",
                                        order = 1,
                                        sets =
                                            persistentListOf(
                                                SetUiModel(
                                                    id = 1,
                                                    targetWeight = 100.0,
                                                    targetReps = 5,
                                                    restInterval = 60,
                                                    notes = "",
                                                    order = 1,
                                                ),
                                                SetUiModel(
                                                    id = 2,
                                                    targetWeight = 100.0,
                                                    targetReps = 5,
                                                    restInterval = 60,
                                                    notes = "",
                                                    order = 2,
                                                ),
                                            ),
                                    ),
                                ),
                        ),
                    lastXpGained = 10,
                ),
            isAmbientMode = false,
            onIntent = {},
        )
    }
}

@Preview(device = SMALL_ROUND)
@Preview(device = LARGE_ROUND)
@Preview(device = RECT)
@Preview(device = SQUARE)
@Composable
fun RestTimerOverlayPreview() {
    MaterialTheme {
        RestTimerOverlay(
            remaining = 30,
            total = 60,
            nextSetInfo = "Bench Press (2/3)",
            isAmbientMode = false,
            onSkip = {},
        )
    }
}
