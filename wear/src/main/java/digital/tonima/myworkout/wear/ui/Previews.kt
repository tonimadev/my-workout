package digital.tonima.myworkout.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.MaterialTheme
import digital.tonima.myworkout.data.model.*

@Preview
@Composable
fun WorkoutListScreenPreview() {
    MaterialTheme {
        WorkoutListScreen(
            workouts = listOf(
                WorkoutWithExercises(
                    workout = WorkoutEntity(id = 1, name = "Workout A"),
                    exercises = listOf(
                        ExerciseWithSets(
                            exercise = ExerciseEntity(id = 1, workoutId = 1, name = "Squat", order = 1),
                            sets = listOf(SetEntity(id = 1, exerciseId = 1, targetWeight = 100.0, targetReps = 5, order = 1))
                        )
                    )
                )
            ),
            onWorkoutClick = {}
        )
    }
}

@Preview
@Composable
fun WorkoutExecutionScreenPreview() {
    MaterialTheme {
        WorkoutExecutionScreen(
            workout = WorkoutWithExercises(
                workout = WorkoutEntity(id = 1, name = "Workout A"),
                exercises = listOf(
                    ExerciseWithSets(
                        exercise = ExerciseEntity(id = 1, workoutId = 1, name = "Squat", order = 1),
                        sets = listOf(
                            SetEntity(id = 1, exerciseId = 1, targetWeight = 100.0, targetReps = 5, order = 1),
                            SetEntity(id = 2, exerciseId = 1, targetWeight = 100.0, targetReps = 5, order = 2)
                        )
                    )
                )
            ),
            activeSession = null,
            restTimeRemaining = 0,
            onCompleteSet = { _, _, _, _, _ -> },
            onFinishSession = {}
        )
    }
}
