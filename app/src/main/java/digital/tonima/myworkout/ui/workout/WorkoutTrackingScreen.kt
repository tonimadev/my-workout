package digital.tonima.myworkout.ui.workout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
                        stringResource(R.string.tracking_title, workout?.workout?.name ?: ""),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.content_description_cancel),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(
                            stringResource(R.string.finish_workout),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(workout.exercises) { exerciseIndex, exercise ->
                        val isLastExercise = exerciseIndex == workout.exercises.size - 1
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                ),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = exercise.exercise.name.uppercase(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                exercise.sets.forEachIndexed { setIndex, set ->
                                    val log = activeSession.logs.find { it.setId == set.id }
                                    val isLogged = log != null
                                    val isLastSet = setIndex == exercise.sets.size - 1
                                    SetRow(
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
                                }
                            }
                        }
                    }
                }
            }

            if (restTimeLeft > 0) {
                val nextSetInfo =
                    remember(workout, activeSession) {
                        if (workout != null && activeSession != null) {
                            var foundNext = ""
                            for (ex in workout.exercises) {
                                val loggedSetIds = activeSession.logs.map { it.setId }
                                val nextSet = ex.sets.find { it.id !in loggedSetIds }
                                if (nextSet != null) {
                                    val setIndex = ex.sets.indexOf(nextSet) + 1
                                    foundNext = "${ex.exercise.name} - $setIndex/${ex.sets.size}"
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
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.rest_interval_label),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (nextSetInfo.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.next_set_label, nextSetInfo),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.height(32.dp))
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                            val progressTarget =
                                if (totalRestTime > 0) {
                                    (restTimeLeft).toFloat() / totalRestTime.toFloat()
                                } else {
                                    0f
                                }
                            val animatedProgress by animateFloatAsState(
                                targetValue = progressTarget,
                                label = "RestTimerProgress",
                            )
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 12.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Text(
                                text = stringResource(R.string.timer_seconds, restTimeLeft),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(48.dp))
                        Button(
                            onClick = onSkipRest,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(
                                stringResource(R.string.action_skip),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetRow(
    setNum: Int,
    targetWeight: Double,
    targetReps: Int,
    isLogged: Boolean,
    actualWeight: Double?,
    actualReps: Int?,
    onLog: (Double, Int) -> Unit,
) {
    var weight by remember { mutableStateOf(targetWeight.toString()) }
    var reps by remember { mutableStateOf(targetReps.toString()) }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = MaterialTheme.shapes.small,
            color = if (isLogged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = setNum.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isLogged) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        OutlinedTextField(
            value = if (isLogged) actualWeight?.toString() ?: "" else weight,
            onValueChange = { weight = it },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.unit_kg)) },
            enabled = !isLogged,
            textStyle = TextStyle(textAlign = TextAlign.Center),
            shape = MaterialTheme.shapes.medium,
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = if (isLogged) actualReps?.toString() ?: "" else reps,
            onValueChange = { reps = it },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.unit_reps)) },
            enabled = !isLogged,
            textStyle = TextStyle(textAlign = TextAlign.Center),
            shape = MaterialTheme.shapes.medium,
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = { onLog(weight.toDoubleOrNull() ?: 0.0, reps.toIntOrNull() ?: 0) },
            enabled = !isLogged,
        ) {
            Icon(
                if (isLogged) Icons.Default.CheckCircle else Icons.Default.Check,
                contentDescription = stringResource(R.string.content_description_log),
                tint = if (isLogged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
