package digital.tonima.myworkout.wear.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    totalRestTime: Long,
    isResting: Boolean,
    isAmbientMode: Boolean,
    onCompleteSet: (Long, Long, Float, Int, Int) -> Unit,
    onFinishSession: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { workout.exercises.size })

    HorizontalPagerScaffold(
        pagerState = pagerState,
        pageIndicator =
            if (isResting) {
                null
            } else {
                { HorizontalPageIndicator(pagerState = pagerState) }
            },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(if (isAmbientMode) Color.Black else MaterialTheme.colorScheme.background),
            ) { pageIndex ->
                val exerciseWithSets = workout.exercises[pageIndex]
                val exercise = exerciseWithSets.exercise
                val sets = exerciseWithSets.sets

                // Find current set for this exercise in the session
                val exerciseSetIds = remember(sets) { sets.map { it.id }.toSet() }
                val logs = activeSession?.logs?.filter { it.setId in exerciseSetIds } ?: emptyList()
                val currentSetIndex = logs.size
                val totalSets = sets.size
                val isLastExercise = pageIndex == workout.exercises.size - 1
                val isLastSet = currentSetIndex == totalSets - 1

                if (currentSetIndex < totalSets) {
                    val currentSet = sets[currentSetIndex]
                    var weight by remember(currentSet.id) { mutableFloatStateOf(currentSet.targetWeight.toFloat()) }
                    var reps by remember(currentSet.id) { mutableIntStateOf(currentSet.targetReps) }

                    ExerciseDetails(
                        exerciseName = exercise.name,
                        setInfo = stringResource(R.string.set_info, currentSetIndex + 1, totalSets),
                        weight = weight,
                        reps = reps,
                        isAmbientMode = isAmbientMode,
                        onWeightChange = { newWeight -> weight = newWeight },
                        onRepsChange = { newReps -> reps = newReps },
                        onCompleteSet = {
                            if (activeSession != null) {
                                val rest = if (isLastExercise && isLastSet) 0 else currentSet.restInterval
                                onCompleteSet(
                                    exercise.id,
                                    currentSet.id,
                                    weight,
                                    reps,
                                    rest,
                                )
                            }
                        },
                    )
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

            if (isResting) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(if (isAmbientMode) Color.Black else MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    RestTimerOverlay(restTimeRemaining, totalRestTime, isAmbientMode)
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
    isAmbientMode: Boolean,
    onWeightChange: (Float) -> Unit,
    onRepsChange: (Int) -> Unit,
    onCompleteSet: () -> Unit,
) {
    val columnState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 24.dp),
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

            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Reps Stepper
            item {
                if (isAmbientMode) {
                    Text(
                        text = stringResource(R.string.reps_count, reps),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Stepper(
                        value = reps,
                        onValueChange = onRepsChange,
                        valueProgression = 1..100,
                        increaseIcon = {
                            Icon(
                                Icons.Default.Add,
                                stringResource(R.string.content_description_increase),
                            )
                        },
                        decreaseIcon = {
                            Icon(
                                Icons.Default.Remove,
                                stringResource(R.string.content_description_decrease),
                            )
                        },
                    ) {
                        Text(stringResource(R.string.reps_count, reps))
                    }
                }
            }

            // Weight
            item {
                if (isAmbientMode) {
                    Text(
                        text = stringResource(R.string.weight_unit, weight.toString()),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
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
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                if (!isAmbientMode) {
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
    }
}

@Composable
fun RestTimerOverlay(
    remaining: Long,
    total: Long,
    isAmbientMode: Boolean,
) {
    val progressTarget = if (total > 0) (total - remaining).toFloat() / total.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "RestTimerProgress",
    )

    val isLowTime = total > 0 && remaining <= (total * 0.1f)
    val indicatorColor = if (isLowTime) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            startAngle = 270f,
            endAngle = -90f,
            strokeWidth = 6.dp,
            gapSize = 0.dp,
            colors =
                ProgressIndicatorDefaults.colors(
                    indicatorColor = if (isAmbientMode) Color.White else indicatorColor,
                    trackColor =
                        if (isAmbientMode) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.1f,
                            )
                        },
                ),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.rest_interval_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.timer_seconds, remaining),
                style = MaterialTheme.typography.displayMedium,
                color = if (isAmbientMode) Color.White else indicatorColor,
            )
        }
    }
}
