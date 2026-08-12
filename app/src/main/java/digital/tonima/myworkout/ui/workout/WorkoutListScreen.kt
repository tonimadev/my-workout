package digital.tonima.myworkout.ui.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import digital.tonima.myworkout.R
import digital.tonima.myworkout.data.model.WorkoutWithExercises

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    workouts: List<WorkoutWithExercises>,
    onWorkoutClick: (Long) -> Unit,
    onAddWorkout: (String) -> Unit,
    onDeleteWorkout: (WorkoutWithExercises) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newWorkoutName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.workout_list_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.content_description_add_workout))
            }
        },
        modifier = modifier
    ) { padding ->
        if (workouts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_workouts_message))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workouts) { workout ->
                    WorkoutItem(
                        workout = workout,
                        onClick = { onWorkoutClick(workout.workout.id) },
                        onDelete = { onDeleteWorkout(workout) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.dialog_add_workout_title)) },
            text = {
                TextField(
                    value = newWorkoutName,
                    onValueChange = { newWorkoutName = it },
                    label = { Text(stringResource(R.string.label_workout_name)) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newWorkoutName.isNotBlank()) {
                        onAddWorkout(newWorkoutName)
                        newWorkoutName = ""
                        showAddDialog = false
                    }
                }) {
                    Text(stringResource(R.string.action_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun WorkoutItem(
    workout: WorkoutWithExercises,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = workout.workout.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.exercises_count, workout.exercises.size),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.content_description_delete))
            }
        }
    }
}
