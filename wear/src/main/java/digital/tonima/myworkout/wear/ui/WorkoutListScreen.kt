package digital.tonima.myworkout.wear.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import digital.tonima.myworkout.wear.R
import digital.tonima.myworkout.data.model.WorkoutWithExercises

@Composable
fun WorkoutListScreen(
    workouts: List<WorkoutWithExercises>,
    onWorkoutClick: (Long) -> Unit
) {
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    
    ScreenScaffold(
        scrollState = columnState
    ) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            contentPadding = contentPadding
        ) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                ) {
                    Text(stringResource(R.string.workout_list_title))
                }
            }
            
            items(workouts.size) { index ->
                val workoutWithExercises = workouts[index]
                val workout = workoutWithExercises.workout
                val itemScope = this
                
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(itemScope, transformationSpec),
                    transformation = itemScope.SurfaceTransformation(transformationSpec),
                    onClick = { onWorkoutClick(workout.id) },
                    label = {
                        Text(
                            text = workout.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = stringResource(R.string.exercises_count, workoutWithExercises.exercises.size),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}
