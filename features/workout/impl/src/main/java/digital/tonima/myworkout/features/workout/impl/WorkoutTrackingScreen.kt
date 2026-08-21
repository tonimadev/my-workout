package digital.tonima.myworkout.features.workout.impl

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.WorkoutWithExercises

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTrackingScreen(
    state: WorkoutState,
    onIntent: (WorkoutIntent) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val workout = state.selectedWorkout
    val activeSession = state.activeSession
    val restTimeLeft = state.restTimeRemaining
    val totalRestTime = state.totalRestTime

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = workout?.workout?.name?.uppercase() ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.content_description_cancel),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
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
                    onClick = { onIntent(WorkoutIntent.FinishWorkout) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .height(72.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.finish_workout).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                }
            }
        },
        modifier = modifier,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (workout == null || activeSession == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 6.dp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    itemsIndexed(
                        items = workout.exercises,
                        key = { _, exercise -> exercise.exercise.id },
                    ) { exerciseIndex, exercise ->
                        val isLastExercise = exerciseIndex == workout.exercises.size - 1

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            elevation =
                                androidx.compose.material3.CardDefaults.elevatedCardElevation(
                                    defaultElevation = 2.dp,
                                ),
                        ) {
                            Column {
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
                                                onIntent(
                                                    WorkoutIntent.LogSet(
                                                        activeSession.session.id,
                                                        exercise.exercise.id,
                                                        set.id,
                                                        weight,
                                                        reps,
                                                        rest,
                                                    ),
                                                )
                                            },
                                        )

                                        if (setIndex < exercise.sets.size - 1) {
                                            Spacer(Modifier.height(16.dp))
                                        }
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
                    onSkip = { onIntent(WorkoutIntent.SkipRest) },
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
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isLogged) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                )
                .border(
                    width = 2.dp,
                    color = if (isLogged) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .background(
                        if (isLogged) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.2f,
                            )
                        },
                        CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = setNum.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = if (isLogged) Color.Black else MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (isLogged) actualWeight?.toString() ?: "" else weightInput,
                    onValueChange = { weightInput = it },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(
                            text = stringResource(R.string.unit_kg).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                        )
                    },
                    enabled = !isLogged,
                    textStyle =
                        TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                        ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        ),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = if (isLogged) actualReps?.toString() ?: "" else repsInput,
                    onValueChange = { repsInput = it },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(
                            text = stringResource(R.string.unit_reps).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                        )
                    },
                    enabled = !isLogged,
                    textStyle =
                        TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                        ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        ),
                    singleLine = true,
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        IconButton(
            onClick = { onLog(weightInput.toDoubleOrNull() ?: 0.0, repsInput.toIntOrNull() ?: 0) },
            enabled = !isLogged,
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isLogged) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    ),
        ) {
            Icon(
                imageVector = if (isLogged) Icons.Default.CheckCircle else Icons.Default.Check,
                contentDescription = null,
                tint = if (isLogged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
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
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {},
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.rest_interval_label).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.outline,
            )

            Spacer(Modifier.height(48.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
                val progressTarget = if (total > 0) remaining.toFloat() / total.toFloat() else 0f
                val animatedProgress by animateFloatAsState(targetValue = progressTarget, label = "RestTimerProgress")

                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 20.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = remaining.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        fontSize = 100.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = stringResource(R.string.seconds_label).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(Modifier.height(64.dp))

            if (nextSetInfo.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "PRÓXIMO:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = nextSetInfo.uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            OutlinedButton(
                onClick = onSkip,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            ) {
                Icon(Icons.Default.Timer, null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.action_skip).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}
