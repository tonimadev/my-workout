package digital.tonima.myworkout.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text =
                                if (selectedExerciseId == null) {
                                    stringResource(R.string.dashboard_pro_title).uppercase()
                                } else {
                                    masterExercises.find { it.id == selectedExerciseId }?.name?.uppercase() ?: ""
                                },
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.5).sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                        if (selectedExerciseId == null) {
                            Text(
                                text = "ACOMPANHE SUA EVOLUÇÃO",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
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
                scrollBehavior = scrollBehavior,
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (selectedExerciseId == null) {
                AthleteDashboard(
                    stats = gamificationStats,
                    achievements = achievements,
                    sessions = sessions,
                    exercises = masterExercises,
                    onSelectExercise = onSelectExercise,
                )
            } else {
                ExerciseStats(
                    exercise = masterExercises.find { it.id == selectedExerciseId },
                    logs = logs,
                )
            }
        }
    }
}

@Composable
fun AthleteDashboard(
    stats: GamificationStats?,
    achievements: List<AchievementEntity>,
    sessions: List<SessionWithLogs>,
    exercises: List<MasterExerciseEntity>,
    onSelectExercise: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(R.string.technical_evolution_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 1.sp,
                )

                exercises.forEach { exercise ->
                    ElevatedCard(
                        onClick = { onSelectExercise(exercise.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors =
                            CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = exercise.name.uppercase(),
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.view_load_history).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            },
                            trailingContent = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    null,
                                    modifier = Modifier.size(16.dp).rotate(180f),
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }
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

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                        MaterialTheme.colorScheme.surface,
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
                        text = stringResource(R.string.level_label, currentLevel).uppercase(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-1).sp,
                    )
                    Text(
                        text = stringResource(R.string.accumulated_xp_label, stats?.totalXp ?: 0).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }

                // Streak Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stats?.currentStreak ?: 0} DIAS",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(16.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.next_level_xp_label, nextLevelXp).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                )
                Text(
                    text = "${(progressTarget * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
fun AchievementSection(achievements: List<AchievementEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.recent_achievements_label).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.outline,
            letterSpacing = 1.sp,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 16.dp),
        ) {
            items(achievements) { achievement ->
                AchievementBadge(achievement)
            }
        }
    }
}

@Composable
fun AchievementBadge(achievement: AchievementEntity) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.width(180.dp),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (achievement.level >= 2) Icons.Default.Star else Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint =
                        if (achievement.level >= 2) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = achievement.name.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 2,
                minLines = 2,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
fun VolumeSection(sessions: List<SessionWithLogs>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.weekly_volume_label).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.outline,
            letterSpacing = 1.sp,
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors =
                CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                val weekVolume = sessions.take(7).map { it.session.totalVolume }
                if (weekVolume.isNotEmpty()) {
                    SimpleBarChart(
                        data = weekVolume.reversed(),
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "ESTIMATIVA DE PROGRESSO SEMANAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.insufficient_data_chart).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
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
    val secondaryColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {
        val spacing = size.width / (data.size * 2 - 1)
        data.forEachIndexed { index, value ->
            val barHeight = (value / max * size.height).toFloat()
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(primaryColor, secondaryColor)),
                topLeft = Offset(index * spacing * 2, size.height - barHeight),
                size = Size(spacing, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            )
        }
    }
}

@Composable
fun ExerciseStats(
    exercise: MasterExerciseEntity?,
    logs: List<WorkoutLogEntity>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "RECORDES PESSOAIS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (logs.isNotEmpty()) {
                                val max = logs.maxBy { it.actualWeight }
                                "${max.actualWeight} KG PARA ${max.actualReps} REPS"
                            } else "SEM DADOS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().height(260.dp),
                shape = RoundedCornerShape(28.dp),
                colors =
                    CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "CARGA MÁXIMA (KG)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 1.sp,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WeightChart(logs = logs, modifier = Modifier.fillMaxSize())
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.load_history_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.outline,
                letterSpacing = 1.sp,
            )
        }

        items(logs.reversed()) { log ->
            val date =
                remember(log.timestamp) {
                    SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                }
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.kg_x_reps, log.actualWeight, log.actualReps).uppercase(),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = date.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

@Composable
fun WeightChart(
    logs: List<WorkoutLogEntity>,
    modifier: Modifier = Modifier,
) {
    if (logs.isEmpty()) return

    val maxWeight = logs.maxOfOrNull { it.actualWeight } ?: 1.0
    val minWeight = logs.minOfOrNull { it.actualWeight } ?: 0.0
    val range = (maxWeight - minWeight).coerceAtLeast(1.0)

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = if (logs.size > 1) width / (logs.size - 1) else width

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
                style = Stroke(width = 8f, cap = StrokeCap.Round),
            )

            // Fill area
            val fillPath =
                Path().apply {
                    addPath(path)
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }
            drawPath(
                path = fillPath,
                brush =
                    Brush.verticalGradient(
                        colors = listOf(primary.copy(alpha = 0.2f), Color.Transparent),
                    ),
            )
        }

        points.forEach { point ->
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = point)
            drawCircle(color = primary, radius = 4.dp.toPx(), center = point)
        }
    }
}
