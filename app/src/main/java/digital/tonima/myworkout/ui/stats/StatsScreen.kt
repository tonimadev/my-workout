package digital.tonima.myworkout.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    masterExercises: List<MasterExerciseEntity>,
    selectedExerciseId: Long?,
    logs: List<WorkoutLogEntity>,
    onSelectExercise: (Long?) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evolução de Carga") },
                navigationIcon = {
                    if (selectedExerciseId != null) {
                        IconButton(onClick = { onSelectExercise(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (selectedExerciseId == null) {
            ExerciseList(
                exercises = masterExercises,
                modifier = Modifier.padding(padding),
                onSelect = onSelectExercise,
            )
        } else {
            ExerciseStats(
                exercise = masterExercises.find { it.id == selectedExerciseId },
                logs = logs,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
fun ExerciseList(
    exercises: List<MasterExerciseEntity>,
    modifier: Modifier = Modifier,
    onSelect: (Long) -> Unit,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(exercises) { exercise ->
            ListItem(
                headlineContent = { Text(exercise.name) },
                modifier = Modifier.clickable { onSelect(exercise.id) },
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun ExerciseStats(
    exercise: MasterExerciseEntity?,
    logs: List<WorkoutLogEntity>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = exercise?.name ?: "Exercício",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (logs.isEmpty()) {
            Text("Nenhum dado registrado para este exercício.")
        } else {
            WeightChart(logs = logs, modifier = Modifier.fillMaxWidth().height(200.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Histórico", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(logs.reversed()) { log ->
                    val date =
                        remember(log.timestamp) {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                        }
                    ListItem(
                        headlineContent = { Text("${log.actualWeight} kg x ${log.actualReps}") },
                        supportingContent = { Text(date) },
                    )
                }
            }
        }
    }
}

@Composable
fun WeightChart(
    logs: List<WorkoutLogEntity>,
    modifier: Modifier = Modifier,
) {
    val maxWeight = logs.maxOfOrNull { it.actualWeight } ?: 1.0
    val minWeight = logs.minOfOrNull { it.actualWeight } ?: 0.0
    val range = (maxWeight - minWeight).coerceAtLeast(1.0)

    val color = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = width / (logs.size - 1).coerceAtLeast(1)

        val points =
            logs.mapIndexed { index, log ->
                val x = index * spacing
                val y = height - ((log.actualWeight - minWeight) / range * height).toFloat()
                Offset(x, y)
            }

        if (points.size > 1) {
            val path =
                Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
            drawPath(path = path, color = color, style = Stroke(width = 4f))
        }

        points.forEach { point ->
            drawCircle(color = color, radius = 6f, center = point)
        }
    }
}
