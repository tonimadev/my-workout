package digital.tonima.myworkout.wear.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.*
import digital.tonima.myworkout.R
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.WorkoutWithExercises

@Composable
fun WorkoutExecutionScreen(
    workout: WorkoutWithExercises,
    activeSession: SessionWithLogs?,
    restTimeRemaining: Long,
    onCompleteSet: (Long, Long, Float, Int, Int) -> Unit,
    onFinishSession: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { workout.exercises.size })

    HorizontalPagerScaffold(
        pagerState = pagerState,
    ) {
        HorizontalPager(
            state = pagerState,
        ) { pageIndex ->
            val exerciseWithSets = workout.exercises[pageIndex]
            val exercise = exerciseWithSets.exercise
            val sets = exerciseWithSets.sets

            // Find current set for this exercise in the session
            val exerciseSetIds = remember(sets) { sets.map { it.id }.toSet() }
            val logs = activeSession?.logs?.filter { it.setId in exerciseSetIds } ?: emptyList()
            val currentSetIndex = logs.size
            val totalSets = sets.size

            if (currentSetIndex < totalSets) {
                val currentSet = sets[currentSetIndex]
                var weight by remember(currentSet.id) { mutableFloatStateOf(currentSet.targetWeight.toFloat()) }
                var reps by remember(currentSet.id) { mutableIntStateOf(currentSet.targetReps) }

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (restTimeRemaining > 0) {
                        RestTimerOverlay(restTimeRemaining)
                    } else {
                        ExerciseDetails(
                            exerciseName = exercise.name,
                            setInfo = stringResource(R.string.set_info, currentSetIndex + 1, totalSets),
                            weight = weight,
                            reps = reps,
                            onWeightChange = { newWeight -> weight = newWeight },
                            onRepsChange = { newReps -> reps = newReps },
                            onCompleteSet = {
                                if (activeSession != null) {
                                    onCompleteSet(
                                        exercise.id,
                                        currentSet.id,
                                        weight,
                                        reps,
                                        currentSet.restInterval,
                                    )
                                }
                            },
                        )
                    }
                }
            } else {
                // Exercise finished
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.exercise_finished, exercise.name),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (pageIndex == workout.exercises.size - 1) {
                        Button(onClick = onFinishSession) {
                            Text(stringResource(R.string.finish_workout))
                        }
                    } else {
                        Text(stringResource(R.string.swipe_next_hint), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseDetails(
    exerciseName: String,
    setInfo: String,
    weight: Float,
    reps: Int,
    onWeightChange: (Float) -> Unit,
    onRepsChange: (Int) -> Unit,
    onCompleteSet: () -> Unit,
) {
    val columnState = rememberTransformingLazyColumnState()
    TransformingLazyColumn(
        state = columnState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 32.dp),
    ) {
        item {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Text(
                text = setInfo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Reps Stepper
        item {
            Stepper(
                value = reps,
                onValueChange = onRepsChange,
                valueProgression = 1..100,
                increaseIcon = { Icon(Icons.Default.Add, stringResource(R.string.content_description_increase)) },
                decreaseIcon = { Icon(Icons.Default.Remove, stringResource(R.string.content_description_decrease)) },
            ) {
                Text(stringResource(R.string.reps_count, reps))
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Weight
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = { onWeightChange(weight - 2.5f) }) {
                    Icon(Icons.Default.Remove, stringResource(R.string.content_description_decrease_weight))
                }
                Text(
                    text = stringResource(R.string.weight_unit, weight.toString()),
                    style = MaterialTheme.typography.bodyMedium,
                )
                IconButton(onClick = { onWeightChange(weight + 2.5f) }) {
                    Icon(Icons.Default.Add, stringResource(R.string.content_description_increase_weight))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Button(
                onClick = onCompleteSet,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Check, stringResource(R.string.content_description_complete))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.action_complete))
            }
        }
    }
}

@Composable
fun RestTimerOverlay(remaining: Long) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.rest_interval_label), style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.timer_seconds, remaining),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        CircularProgressIndicator()
    }
}
