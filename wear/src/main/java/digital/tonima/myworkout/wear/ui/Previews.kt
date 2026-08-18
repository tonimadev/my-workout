package digital.tonima.myworkout.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices.LARGE_ROUND
import androidx.wear.tooling.preview.devices.WearDevices.RECT
import androidx.wear.tooling.preview.devices.WearDevices.SMALL_ROUND
import androidx.wear.tooling.preview.devices.WearDevices.SQUARE
import digital.tonima.myworkout.data.model.ExerciseEntity
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises

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
                        listOf(
                            WorkoutWithExercises(
                                workout = WorkoutEntity(id = 1, name = "Workout A"),
                                exercises =
                                    listOf(
                                        ExerciseWithSets(
                                            exercise = ExerciseEntity(id = 1, workoutId = 1, name = "Squat", order = 1),
                                            sets =
                                                listOf(
                                                    SetEntity(
                                                        id = 1,
                                                        exerciseId = 1,
                                                        targetWeight = 100.0,
                                                        targetReps = 5,
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
                        WorkoutWithExercises(
                            workout = WorkoutEntity(id = 1, name = "Workout A"),
                            exercises =
                                listOf(
                                    ExerciseWithSets(
                                        exercise = ExerciseEntity(id = 1, workoutId = 1, name = "Squat", order = 1),
                                        sets =
                                            listOf(
                                                SetEntity(
                                                    id = 1,
                                                    exerciseId = 1,
                                                    targetWeight = 100.0,
                                                    targetReps = 5,
                                                    order = 1,
                                                ),
                                                SetEntity(
                                                    id = 2,
                                                    exerciseId = 1,
                                                    targetWeight = 100.0,
                                                    targetReps = 5,
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
