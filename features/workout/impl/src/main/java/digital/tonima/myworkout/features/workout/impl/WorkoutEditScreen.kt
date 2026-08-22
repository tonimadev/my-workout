package digital.tonima.myworkout.features.workout.impl

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.SetEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditScreen(
    state: WorkoutState,
    onIntent: (WorkoutIntent) -> Unit,
    onBack: () -> Unit,
    onStartWorkout: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val workout = state.selectedWorkout
    val context = LocalContext.current
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }
    var editingSet by remember { mutableStateOf<Pair<Long, SetEntity>?>(null) }

    LaunchedEffect(state.shareText) {
        state.shareText?.let { text ->
            val sendIntent =
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
            onIntent(WorkoutIntent.ClearShareData)
        }
    }

    LaunchedEffect(state.exportJson) {
        state.exportJson?.let { json ->
            val sendIntent =
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, json)
                    type = "text/plain"
                }
            val shareIntent = Intent.createChooser(sendIntent, "Export Workout JSON")
            context.startActivity(shareIntent)
            onIntent(WorkoutIntent.ClearShareData)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text =
                            workout?.workout?.name?.uppercase() ?: stringResource(
                                R.string.workout_details,
                            ).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back),
                        )
                    }
                },
                actions = {
                    if (workout != null) {
                        IconButton(onClick = { onIntent(WorkoutIntent.ShareWorkout(workout)) }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = stringResource(R.string.action_share),
                            )
                        }
                        IconButton(onClick = { onIntent(WorkoutIntent.ExportWorkout(workout)) }) {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = stringResource(R.string.action_export),
                            )
                        }
                        IconButton(
                            onClick = { onStartWorkout(workout.workout.id) },
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier =
                                    Modifier.size(
                                        28.dp,
                                    ),
                            )
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        modifier = modifier,
    ) { padding ->
        if (workout == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(workout.exercises) { exercise ->
                    ExerciseSection(
                        exercise = exercise,
                        workoutId = workout.workout.id,
                        onAddSet = { wId, eId -> onIntent(WorkoutIntent.AddSet(wId, eId)) },
                        onDeleteSet = { wId, eId, sId -> onIntent(WorkoutIntent.DeleteSet(wId, eId, sId)) },
                        onEditSet = { editingSet = it },
                        onDuplicate = { wId, eId -> onIntent(WorkoutIntent.DuplicateExercise(wId, eId)) },
                        onDeleteExercise = { wId, eId -> onIntent(WorkoutIntent.DeleteExercise(wId, eId)) },
                    )
                }
                item {
                    Button(
                        onClick = { showAddExerciseDialog = true },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.action_add_exercise).uppercase(),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }
        }
    }

    if (showAddExerciseDialog && workout != null) {
        AddExerciseDialog(
            name = newExerciseName,
            onNameChange = { newExerciseName = it },
            onDismiss = { showAddExerciseDialog = false },
            onConfirm = {
                if (newExerciseName.isNotBlank()) {
                    onIntent(WorkoutIntent.AddExercise(workout.workout.id, newExerciseName))
                    newExerciseName = ""
                    showAddExerciseDialog = false
                }
            },
        )
    }

    editingSet?.let { (exerciseId, set) ->
        EditSetDialog(
            set = set,
            onDismiss = { editingSet = null },
            onSave = { w, r, restInt ->
                workout?.workout?.id?.let { workoutId ->
                    onIntent(WorkoutIntent.UpdateSet(workoutId, exerciseId, set.id, w, r, restInt))
                }
                editingSet = null
            },
        )
    }
}

@Composable
fun ExerciseSection(
    exercise: ExerciseWithSets,
    workoutId: Long,
    onAddSet: (Long, Long) -> Unit,
    onDeleteSet: (Long, Long, Long) -> Unit,
    onEditSet: (Pair<Long, SetEntity>) -> Unit,
    onDuplicate: (Long, Long) -> Unit,
    onDeleteExercise: (Long, Long) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors =
            androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            // Header
            Row(
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = exercise.exercise.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    letterSpacing = 0.5.sp,
                )

                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Black)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.action_duplicate).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            onClick = {
                                onDuplicate(workoutId, exercise.exercise.id)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.action_delete).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                onDeleteExercise(workoutId, exercise.exercise.id)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                exercise.sets.forEachIndexed { index, set ->
                    SetItemRow(
                        index = index,
                        set = set,
                        onEdit = { onEditSet(exercise.exercise.id to set) },
                        onDelete = { onDeleteSet(workoutId, exercise.exercise.id, set.id) },
                    )
                    if (index < exercise.sets.size - 1) {
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { onAddSet(workoutId, exercise.exercise.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.action_add_set).uppercase(), fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun SetItemRow(
    index: Int,
    set: SetEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${set.targetWeight} KG X ${set.targetReps}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${stringResource(R.string.rest_interval_label).uppercase()}: ${set.restInterval}S",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun AddExerciseDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_add_exercise_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.label_exercise_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.action_add).uppercase(), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel).uppercase(), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
    )
}

@Composable
fun EditSetDialog(
    set: SetEntity,
    onDismiss: () -> Unit,
    onSave: (Double, Int, Int) -> Unit,
) {
    var weight by remember { mutableStateOf(set.targetWeight.toString()) }
    var reps by remember { mutableStateOf(set.targetReps.toString()) }
    var rest by remember { mutableStateOf(set.restInterval.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_edit_set_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.label_weight_kg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text(stringResource(R.string.label_reps)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
                OutlinedTextField(
                    value = rest,
                    onValueChange = { rest = it },
                    label = { Text(stringResource(R.string.label_rest_interval)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weight.toDoubleOrNull() ?: set.targetWeight
                    val r = reps.toIntOrNull() ?: set.targetReps
                    val rs = rest.toIntOrNull() ?: set.restInterval
                    onSave(w, r, rs)
                },
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.action_save).uppercase(), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel).uppercase(), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
    )
}
