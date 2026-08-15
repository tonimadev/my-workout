package digital.tonima.myworkout.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import digital.tonima.myworkout.data.model.AchievementEntity
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.preferences.GamificationStats
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    masterExercises: List<MasterExerciseEntity>,
    selectedExerciseId: Long?,
    logs: List<WorkoutLogEntity>,
    gamificationStats: GamificationStats?,
    achievements: List<AchievementEntity>,
    sessions: List<SessionWithLogs>,
    onSelectExercise: (Long?) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evolução do Atleta") },
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
            AthleteDashboard(
                stats = gamificationStats,
                achievements = achievements,
                sessions = sessions,
                exercises = masterExercises,
                modifier = Modifier.padding(padding),
                onSelectExercise = onSelectExercise,
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
fun AthleteDashboard(
    stats: GamificationStats?,
    achievements: List<AchievementEntity>,
    sessions: List<SessionWithLogs>,
    exercises: List<MasterExerciseEntity>,
    modifier: Modifier = Modifier,
    onSelectExercise: (Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            GamificationHeader(stats)
        }

        if (achievements.isNotEmpty()) {
            item {
                AchievementSection(achievements)
            }
        }

        item {
            VolumeSection(sessions)
        }

        item {
            Text("Evolução por Exercício", style = MaterialTheme.typography.titleLarge)
        }

        items(exercises) { exercise ->
            Card(
                onClick = { onSelectExercise(exercise.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                ListItem(
                    headlineContent = { Text(exercise.name) },
                    supportingContent = { Text("Ver progresso") },
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GamificationHeader(stats: GamificationStats?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Nível ${stats?.currentLevel ?: 1}", style = MaterialTheme.typography.headlineSmall)
                    Text("${stats?.totalXp ?: 0} XP Total", style = MaterialTheme.typography.bodyMedium)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${stats?.currentStreak ?: 0} DIAS",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val currentLevel = stats?.currentLevel ?: 1
            val nextLevelXp = (currentLevel * currentLevel) * 100
            val currentLevelXp = ((currentLevel - 1) * (currentLevel - 1)) * 100
            val progress =
                if (nextLevelXp > currentLevelXp) {
                    ((stats?.totalXp ?: 0) - currentLevelXp).toFloat() / (nextLevelXp - currentLevelXp)
                } else {
                    0f
                }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Faltam ${(nextLevelXp - (stats?.totalXp ?: 0)).coerceAtLeast(0)} XP para o próximo nível",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
fun AchievementSection(achievements: List<AchievementEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Conquistas Recentes", style = MaterialTheme.typography.titleLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(achievements) { achievement ->
                AchievementBadge(achievement)
            }
        }
    }
}

@Composable
fun AchievementBadge(achievement: AchievementEntity) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(140.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (achievement.level >= 2) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                achievement.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                fontWeight = FontWeight.Bold,
            )
            Text(
                achievement.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                minLines = 2,
            )
        }
    }
}

@Composable
fun VolumeSection(sessions: List<SessionWithLogs>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Volume Semanal", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                val weekVolume =
                    sessions.take(7).map { it.session.totalVolume }
                if (weekVolume.isNotEmpty()) {
                    SimpleBarChart(
                        data = weekVolume.reversed(),
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                    )
                } else {
                    Text("Dados insuficientes para gerar o gráfico.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<Double>,
    modifier: Modifier = Modifier,
) {
    val max = (data.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val barWidth = size.width / (data.size * 2 - 1)
        data.forEachIndexed { index, value ->
            val barHeight = (value / max * size.height).toFloat()
            drawRect(
                color = primaryColor,
                topLeft = Offset(index * barWidth * 2, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
            )
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
