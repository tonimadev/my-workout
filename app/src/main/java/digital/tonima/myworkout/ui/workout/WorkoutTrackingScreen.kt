package digital.tonima.myworkout.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import digital.tonima.myworkout.R
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTrackingScreen(
    workout: WorkoutWithExercises?,
    activeSession: SessionWithLogs?,
    onLogSet: (Long, Long, Long, Double, Int) -> Unit,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var restTimeLeft by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(restTimeLeft) {
        if (restTimeLeft > 0) {
            delay(1000L)
            restTimeLeft -= 1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tracking_title, workout?.workout?.name ?: "")) },
                actions = {
                    if (restTimeLeft > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null)
                            Text(text = " " + stringResource(R.string.timer_seconds, restTimeLeft), style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(16.dp))
                        }
                    }
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.content_description_cancel))
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(stringResource(R.string.finish_workout))
                }
            }
        },
        modifier = modifier
    ) { padding ->
        if (workout == null || activeSession == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(workout.exercises) { exercise ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = exercise.exercise.name, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(8.dp))
                            exercise.sets.forEach { set ->
                                val isLogged = activeSession.logs.any { it.setId == set.id }
                                SetRow(
                                    setNum = exercise.sets.indexOf(set) + 1,
                                    targetWeight = set.targetWeight,
                                    targetReps = set.targetReps,
                                    isLogged = isLogged,
                                    onLog = { weight, reps ->
                                        onLogSet(activeSession.session.id, exercise.exercise.id, set.id, weight, reps)
                                        restTimeLeft = set.restInterval
                                    }
                                )
                            }
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
    onLog: (Double, Int) -> Unit
) {
    var weight by remember { mutableStateOf(targetWeight.toString()) }
    var reps by remember { mutableStateOf(targetReps.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.set_label, setNum), modifier = Modifier.width(60.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.unit_kg)) },
            enabled = !isLogged
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.unit_reps)) },
            enabled = !isLogged
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = { onLog(weight.toDoubleOrNull() ?: 0.0, reps.toIntOrNull() ?: 0) },
            enabled = !isLogged
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = stringResource(R.string.content_description_log),
                tint = if (isLogged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}
