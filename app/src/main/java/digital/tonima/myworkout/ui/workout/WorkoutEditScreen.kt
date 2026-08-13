package digital.tonima.myworkout.ui.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import digital.tonima.myworkout.R
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditScreen(
    workout: WorkoutWithExercises?,
    onBack: () -> Unit,
    onStartWorkout: (Long) -> Unit,
    onAddExercise: (Long, String) -> Unit,
    onAddSet: (Long, Long) -> Unit,
    onUpdateSet: (Long, Long, Long, Double, Int, Int) -> Unit,
    onDeleteSet: (Long, Long, Long) -> Unit,
    onDuplicateExercise: (Long, Long) -> Unit,
    onDeleteExercise: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }
    var editingSet by remember { mutableStateOf<Pair<Long, SetEntity>?>(null) }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { 
                    Text(
                        workout?.workout?.name ?: stringResource(R.string.workout_details),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_description_back))
                    }
                },
                actions = {
                    if (workout != null) {
                        Button(
                            onClick = { onStartWorkout(workout.workout.id) },
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.action_start), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        if (workout == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(workout.exercises) { exercise ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${exercise.exercise.order + 1}. ${exercise.exercise.name}".uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                var showExerciseMenu by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { showExerciseMenu = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = null)
                                    }
                                    DropdownMenu(
                                        expanded = showExerciseMenu,
                                        onDismissRequest = { showExerciseMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.action_duplicate)) },
                                            onClick = {
                                                onDuplicateExercise(workout.workout.id, exercise.exercise.id)
                                                showExerciseMenu = false
                                            },
                                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.action_delete)) },
                                            onClick = {
                                                onDeleteExercise(workout.workout.id, exercise.exercise.id)
                                                showExerciseMenu = false
                                            },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                                        )
                                    }
                                }
                            }
                            exercise.sets.forEachIndexed { index, set ->
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { editingSet = exercise.exercise.id to set }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(24.dp),
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = (index + 1).toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "${set.targetWeight}kg x ${set.targetReps} reps (${set.restInterval}s rest)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { onDeleteSet(workout.workout.id, exercise.exercise.id, set.id) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.action_delete_set),
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = { onAddSet(workout.workout.id, exercise.exercise.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text(stringResource(R.string.action_add_set), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { showAddExerciseDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.action_add_exercise), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddExerciseDialog && workout != null) {
        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            title = { Text(stringResource(R.string.dialog_add_exercise_title)) },
            text = {
                TextField(
                    value = newExerciseName,
                    onValueChange = { newExerciseName = it },
                    label = { Text(stringResource(R.string.label_exercise_name)) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newExerciseName.isNotBlank()) {
                        onAddExercise(workout.workout.id, newExerciseName)
                        newExerciseName = ""
                        showAddExerciseDialog = false
                    }
                }) {
                    Text(stringResource(R.string.action_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    editingSet?.let { (exerciseId, set) ->
        var weight by remember { mutableStateOf(set.targetWeight.toString()) }
        var reps by remember { mutableStateOf(set.targetReps.toString()) }
        var rest by remember { mutableStateOf(set.restInterval.toString()) }

        AlertDialog(
            onDismissRequest = { editingSet = null },
            title = { Text(stringResource(R.string.dialog_edit_set_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text(stringResource(R.string.label_weight_kg)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text(stringResource(R.string.label_reps)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rest,
                        onValueChange = { rest = it },
                        label = { Text(stringResource(R.string.label_rest_interval)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val w = weight.toDoubleOrNull() ?: set.targetWeight
                    val r = reps.toIntOrNull() ?: set.targetReps
                    val restInt = rest.toIntOrNull() ?: set.restInterval
                    workout?.workout?.id?.let { workoutId ->
                        onUpdateSet(workoutId, exerciseId, set.id, w, r, restInt)
                    }
                    editingSet = null
                }) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSet = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
