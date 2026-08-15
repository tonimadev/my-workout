package digital.tonima.myworkout.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults.colors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.tonima.myworkout.R
import digital.tonima.myworkout.data.model.AchievementEntity
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.preferences.GamificationStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.dashboard_pro_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                    )
                },
                navigationIcon = {
                    if (selectedExerciseId != null) {
                        IconButton(onClick = { onSelectExercise(null) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_description_back),
                            )
                        }
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
        verticalArrangement = Arrangement.spacedBy(28.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
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
            Text(
                stringResource(R.string.technical_evolution_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
            )
        }

        items(exercises) { exercise ->
            Surface(
                onClick = { onSelectExercise(exercise.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                ListItem(
                    headlineContent = {
                        Text(exercise.name.uppercase(), fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                    },
                    supportingContent = { Text(stringResource(R.string.view_load_history)) },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null,
                            modifier = Modifier.size(16.dp).rotate(180f),
                        )
                    },
                    colors = colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

@Composable
fun GamificationHeader(stats: GamificationStats?) {
    val currentLevel = stats?.currentLevel ?: 1
    val nextLevelXp = (currentLevel * currentLevel) * 100
    val currentLevelXp = ((currentLevel - 1) * (currentLevel - 1)) * 100
    val xpInLevel = (stats?.totalXp ?: 0) - currentLevelXp
    val xpRequired = nextLevelXp - currentLevelXp

    val progressTarget = if (xpRequired > 0) xpInLevel.toFloat() / xpRequired else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1500),
        label = "XpProgress",
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.background,
                                ),
                        ),
                )
                .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    stringResource(R.string.level_label, currentLevel),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    stringResource(R.string.accumulated_xp_label, stats?.totalXp ?: 0),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Streak Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondary,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${stats?.currentStreak ?: 0}D",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(
                            brush =
                                Brush.horizontalGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary,
                                        ),
                                ),
                        ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.next_level_xp_label, nextLevelXp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                "${(progressTarget * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
fun AchievementSection(achievements: List<AchievementEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.recent_achievements_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(achievements) { achievement ->
                AchievementBadge(achievement)
            }
        }
    }
}

@Composable
fun AchievementBadge(achievement: AchievementEntity) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(160.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                if (achievement.level >= 2) Icons.Default.Star else Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint =
                    if (achievement.level >= 2) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                achievement.name.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 2,
                minLines = 2,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun VolumeSection(sessions: List<SessionWithLogs>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.weekly_volume_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val weekVolume = sessions.take(7).map { it.session.totalVolume }
                if (weekVolume.isNotEmpty()) {
                    SimpleBarChart(
                        data = weekVolume.reversed(),
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                    )
                } else {
                    Text(stringResource(R.string.insufficient_data_chart), style = MaterialTheme.typography.bodyMedium)
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
        val spacing = size.width / (data.size * 2 - 1)
        data.forEachIndexed { index, value ->
            val barHeight = (value / max * size.height).toFloat()
            drawRect(
                color = primaryColor,
                topLeft = Offset(index * spacing * 2, size.height - barHeight),
                size = Size(spacing, barHeight),
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
            text = exercise?.name?.uppercase() ?: stringResource(R.string.default_exercise_name),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp,
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (logs.isEmpty()) {
            Text(stringResource(R.string.no_data_exercise))
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                WeightChart(logs = logs, modifier = Modifier.fillMaxSize().padding(16.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                stringResource(R.string.load_history_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logs.reversed()) { log ->
                    val date =
                        remember(log.timestamp) {
                            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                        }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    stringResource(R.string.kg_x_reps, log.actualWeight, log.actualReps),
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            supportingContent = { Text(date.uppercase(), style = MaterialTheme.typography.labelSmall) },
                            colors = colors(containerColor = Color.Transparent),
                        )
                    }
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

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

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
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(listOf(primary, tertiary)),
                style = Stroke(width = 6f),
            )
        }

        points.forEach { point ->
            drawCircle(color = Color.White, radius = 8f, center = point)
            drawCircle(color = primary, radius = 4f, center = point)
        }
    }
}
