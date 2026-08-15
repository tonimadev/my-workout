package digital.tonima.myworkout.wear.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.HorizontalPagerScaffold
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Stepper
import androidx.wear.compose.material3.Text
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
    xpGained: Int?,
    onCompleteSet: (Long, Long, Float, Int, Int) -> Unit,
    onSkipRest: () -> Unit,
    onFinishSession: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { workout.exercises.size })

    val currentExerciseIndex = pagerState.currentPage
    val currentExercise = workout.exercises.getOrNull(currentExerciseIndex)

    LaunchedEffect(isResting, currentExerciseIndex, activeSession?.logs) {
        if (!isResting && currentExercise != null) {
            val exerciseSetIds = currentExercise.sets.map { it.id }.toSet()
            val logsCount = activeSession?.logs?.count { it.setId in exerciseSetIds } ?: 0
            if (logsCount >= currentExercise.sets.size && currentExerciseIndex < workout.exercises.size - 1) {
                pagerState.animateScrollToPage(currentExerciseIndex + 1)
            }
        }
    }

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
                        onFinishSession = onFinishSession,
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

            AnimatedVisibility(
                visible = xpGained != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.xp_gained_feedback, xpGained ?: 0),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (isResting) {
                val nextSetInfo =
                    remember(workout, activeSession) {
                        if (activeSession != null) {
                            var foundNext = ""
                            for (ex in workout.exercises) {
                                val loggedSetIds = activeSession.logs.map { it.setId }
                                val nextSet = ex.sets.find { it.id !in loggedSetIds }
                                if (nextSet != null) {
                                    val setIndex = ex.sets.indexOf(nextSet) + 1
                                    foundNext = "${ex.exercise.name} ($setIndex/${ex.sets.size})"
                                    break
                                }
                            }
                            foundNext
                        } else {
                            ""
                        }
                    }

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(if (isAmbientMode) Color.Black else MaterialTheme.colorScheme.background)
                            .pointerInput(Unit) {},
                    contentAlignment = Alignment.Center,
                ) {
                    RestTimerOverlay(
                        remaining = restTimeRemaining,
                        total = totalRestTime,
                        nextSetInfo = nextSetInfo,
                        isAmbientMode = isAmbientMode,
                        onSkip = onSkipRest,
                    )
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
    onFinishSession: () -> Unit,
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

            // Weight Control
            item {
                if (isAmbientMode) {
                    Text(
                        text = stringResource(R.string.weight_unit, weight.toString()),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.label_weight),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            IconButton(onClick = { onWeightChange(weight - 0.5f) }) {
                                Icon(Icons.Default.Remove, null)
                            }
                            Text(
                                text = weight.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 12.dp),
                            )
                            IconButton(onClick = { onWeightChange(weight + 0.5f) }) {
                                Icon(Icons.Default.Add, null)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Reps Control
            item {
                if (isAmbientMode) {
                    Text(
                        text = stringResource(R.string.reps_count, reps),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.label_reps_uppercase),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Stepper(
                            value = reps,
                            onValueChange = onRepsChange,
                            valueProgression = 1..100,
                            increaseIcon = { Icon(Icons.Default.Add, null) },
                            decreaseIcon = { Icon(Icons.Default.Remove, null) },
                        ) {
                            Text(reps.toString(), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

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

            item {
                if (!isAmbientMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.wear.compose.material3.TextButton(
                        onClick = onFinishSession,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.finish_workout),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
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
    nextSetInfo: String,
    isAmbientMode: Boolean,
    onSkip: () -> Unit,
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
            if (nextSetInfo.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.next_set_label, nextSetInfo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.timer_seconds, remaining),
                style = MaterialTheme.typography.displayMedium,
                color = if (isAmbientMode) Color.White else indicatorColor,
            )
            if (!isAmbientMode) {
                Spacer(modifier = Modifier.height(4.dp))
                androidx.wear.compose.material3.TextButton(onClick = onSkip) {
                    Text(
                        text = stringResource(R.string.action_skip),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
