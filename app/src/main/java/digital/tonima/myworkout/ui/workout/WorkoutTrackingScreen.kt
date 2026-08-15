package digital.tonima.myworkout.ui.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.colors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.tonima.myworkout.R
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.WorkoutWithExercises

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTrackingScreen(
    workout: WorkoutWithExercises?,
    activeSession: SessionWithLogs?,
    restTimeLeft: Int,
    totalRestTime: Int,
    onLogSet: (Long, Long, Long, Double, Int, Int) -> Unit,
    onSkipRest: () -> Unit,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        workout?.workout?.name?.uppercase() ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.content_description_cancel),
                        )
                    }
                },
                colors =
                    topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = Color.Unspecified,
                        navigationIconContentColor = Color.Unspecified,
                        titleContentColor = Color.Unspecified,
                        actionIconContentColor = Color.Unspecified,
                    ),
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
            ) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(
                        stringResource(R.string.finish_workout).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        },
        modifier = modifier,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (workout == null || activeSession == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    itemsIndexed(workout.exercises) { exerciseIndex, exercise ->
                        val isLastExercise = exerciseIndex == workout.exercises.size - 1

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                        RoundedCornerShape(28.dp),
                                    ),
                        ) {
                            // Exercise Header
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .background(
                                            brush =
                                                Brush.horizontalGradient(
                                                    colors =
                                                        listOf(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                                        ),
                                                ),
                                        )
                                        .padding(16.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = exercise.exercise.name.uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        letterSpacing = 0.5.sp,
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                exercise.sets.forEachIndexed { setIndex, set ->
                                    val log = activeSession.logs.find { it.setId == set.id }
                                    val isLogged = log != null
                                    val isLastSet = setIndex == exercise.sets.size - 1

                                    SetTrackingRow(
                                        setNum = setIndex + 1,
                                        targetWeight = set.targetWeight,
                                        targetReps = set.targetReps,
                                        isLogged = isLogged,
                                        actualWeight = log?.actualWeight,
                                        actualReps = log?.actualReps,
                                        onLog = { weight, reps ->
                                            val rest = if (isLastExercise && isLastSet) 0 else set.restInterval
                                            onLogSet(
                                                activeSession.session.id,
                                                exercise.exercise.id,
                                                set.id,
                                                weight,
                                                reps,
                                                rest,
                                            )
                                        },
                                    )

                                    if (setIndex < exercise.sets.size - 1) {
                                        Spacer(Modifier.height(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = restTimeLeft > 0,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                RestTimerOverlay(
                    remaining = restTimeLeft,
                    total = totalRestTime,
                    workout = workout,
                    activeSession = activeSession,
                    onSkip = onSkipRest,
                )
            }
        }
    }
}

@Composable
fun SetTrackingRow(
    setNum: Int,
    targetWeight: Double,
    targetReps: Int,
    isLogged: Boolean,
    actualWeight: Double?,
    actualReps: Int?,
    onLog: (Double, Int) -> Unit,
) {
    var weightInput by remember { mutableStateOf(targetWeight.toString()) }
    var repsInput by remember { mutableStateOf(targetReps.toString()) }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isLogged) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                    },
                )
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .background(
                        if (isLogged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = setNum.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isLogged) Color.Black else MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.width(12.dp))

        OutlinedTextField(
            value =
                if (isLogged) {
                    actualWeight?.toString() ?: ""
                } else {
                    weightInput
                },
            onValueChange = { weightInput = it },
            modifier = Modifier.weight(1f).height(52.dp),
            placeholder = {
                Text(
                    stringResource(R.string.unit_kg).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                )
            },
            enabled = !isLogged,
            textStyle = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors =
                colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    disabledBorderColor = Color.Transparent,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                ),
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value =
                if (isLogged) {
                    actualReps?.toString() ?: ""
                } else {
                    repsInput
                },
            onValueChange = { repsInput = it },
            modifier = Modifier.weight(1f).height(52.dp),
            placeholder = {
                Text(
                    stringResource(R.string.unit_reps).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                )
            },
            enabled = !isLogged,
            textStyle = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors =
                colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    disabledBorderColor = Color.Transparent,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                ),
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = { onLog(weightInput.toDoubleOrNull() ?: 0.0, repsInput.toIntOrNull() ?: 0) },
            enabled = !isLogged,
            modifier = Modifier.size(44.dp),
        ) {
            Icon(
                if (isLogged) Icons.Default.CheckCircle else Icons.Default.Check,
                contentDescription = null,
                tint = if (isLogged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
fun RestTimerOverlay(
    remaining: Int,
    total: Int,
    workout: WorkoutWithExercises?,
    activeSession: SessionWithLogs?,
    onSkip: () -> Unit,
) {
    val nextSetInfo =
        remember(workout, activeSession) {
            if (workout != null && activeSession != null) {
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

    Surface(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {},
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(R.string.rest_interval_label).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.outline,
            )

            Spacer(Modifier.height(48.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                val progressTarget = if (total > 0) remaining.toFloat() / total.toFloat() else 0f
                val animatedProgress by animateFloatAsState(targetValue = progressTarget, label = "RestTimerProgress")

                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 16.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = remaining.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        fontSize = 80.sp,
                    )
                    Text(
                        stringResource(R.string.seconds_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            if (nextSetInfo.isNotEmpty()) {
                Text(
                    "PRÓXIMO:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    nextSetInfo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(64.dp))

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            ) {
                Icon(Icons.Default.Timer, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_skip).uppercase(), fontWeight = FontWeight.Black)
            }
        }
    }
}
