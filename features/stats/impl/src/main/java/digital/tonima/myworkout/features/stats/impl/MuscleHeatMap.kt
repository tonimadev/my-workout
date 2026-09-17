package digital.tonima.myworkout.features.stats.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.tonima.myworkout.data.model.BodyView
import digital.tonima.myworkout.data.model.MuscleGroup
import digital.tonima.myworkout.ui.components.musclebody.MuscleBodyDiagram
import digital.tonima.myworkout.ui.components.musclebody.MuscleHighlightMode
import digital.tonima.myworkout.ui.model.MasterExerciseUiModel
import digital.tonima.myworkout.ui.model.SessionUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap

@Composable
fun MuscleHeatMapSection(
    sessions: ImmutableList<SessionUiModel>,
    exercises: ImmutableList<MasterExerciseUiModel>,
) {
    val intensityByMuscle = remember(sessions, exercises) { aggregateVolumeByMuscle(sessions, exercises) }
    var bodyView by remember { mutableStateOf(BodyView.FRONT) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.muscle_heatmap_label).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                MuscleBodyDiagram(
                    view = bodyView,
                    onViewToggle = { bodyView = it },
                    intensityByMuscle = intensityByMuscle,
                    highlightMode = MuscleHighlightMode.INTENSITY,
                    interactive = false,
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                )
            }
        }
    }
}

/** Sums actualWeight*actualReps per muscle (secondary muscles at half weight), normalized 0f..1f. */
private fun aggregateVolumeByMuscle(
    sessions: ImmutableList<SessionUiModel>,
    exercises: ImmutableList<MasterExerciseUiModel>,
): ImmutableMap<MuscleGroup, Float> {
    val exerciseById = exercises.associateBy { it.id }
    val rawVolume = mutableMapOf<MuscleGroup, Double>()
    sessions.forEach { session ->
        session.logs.forEach { log ->
            val exercise = exerciseById[log.exerciseId] ?: return@forEach
            val volume = log.actualWeight * log.actualReps
            exercise.primaryMuscle?.let { rawVolume.merge(it, volume, Double::plus) }
            exercise.secondaryMuscles.forEach { rawVolume.merge(it, volume * 0.5, Double::plus) }
        }
    }
    val max = rawVolume.values.maxOrNull()?.takeIf { it > 0 } ?: return persistentMapOf()
    return rawVolume.mapValues { (_, volume) -> (volume / max).toFloat() }.toPersistentMap()
}
