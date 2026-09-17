package digital.tonima.myworkout.ui.components.musclebody

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import digital.tonima.myworkout.data.model.BodyView
import digital.tonima.myworkout.data.model.MuscleGroup
import digital.tonima.myworkout.ui.util.labelRes
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

enum class MuscleHighlightMode { NONE, DISCRETE, INTENSITY }

/**
 * A stylized, schematic human body diagram drawn entirely with [Canvas]/[androidx.compose.ui.graphics.Path]
 * (no bitmap/vector assets), used to visualize which [MuscleGroup] a given exercise, or a period of
 * training, works.
 *
 * @param view which half of the body ([BodyView.FRONT] or [BodyView.BACK]) to render right now.
 * @param onViewToggle when non-null, shows a front/back segmented toggle above the diagram.
 * @param primaryMuscle/[secondaryMuscles] used when [highlightMode] is [MuscleHighlightMode.DISCRETE].
 * @param intensityByMuscle 0f..1f per muscle, used when [highlightMode] is [MuscleHighlightMode.INTENSITY].
 * @param interactive when true, tapping a muscle region invokes [onMuscleSelected].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleBodyDiagram(
    view: BodyView,
    onViewToggle: ((BodyView) -> Unit)? = null,
    primaryMuscle: MuscleGroup? = null,
    secondaryMuscles: ImmutableList<MuscleGroup> = persistentListOf(),
    intensityByMuscle: ImmutableMap<MuscleGroup, Float> = persistentMapOf(),
    highlightMode: MuscleHighlightMode = MuscleHighlightMode.DISCRETE,
    interactive: Boolean = false,
    onMuscleSelected: ((MuscleGroup) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val baseFillColor = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val density = LocalDensity.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (onViewToggle != null) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                BodyView.entries.forEachIndexed { index, entry ->
                    SegmentedButton(
                        selected = view == entry,
                        onClick = { onViewToggle(entry) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = BodyView.entries.size),
                    ) {
                        Text(stringResource(entry.labelRes()).uppercase(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        BoxWithConstraints(modifier = modifier) {
            // A uniform scale (instead of independent X/Y) keeps the body's true proportions no
            // matter the container's aspect ratio; the drawing is then centered in the extra space.
            val scale = minOf(constraints.maxWidth / BODY_WIDTH, constraints.maxHeight / BODY_HEIGHT)
            val offsetX = (constraints.maxWidth - BODY_WIDTH * scale) / 2f
            val offsetY = (constraints.maxHeight - BODY_HEIGHT * scale) / 2f
            val parts = bodyPartsFor(view)

            Canvas(modifier = Modifier.fillMaxSize()) {
                translate(left = offsetX, top = offsetY) {
                    scale(scale = scale, pivot = Offset.Zero) {
                        val strokeWidth = 1f / scale
                        parts.forEach { part ->
                            val fillColor =
                                colorForMuscle(
                                    muscle = part.muscle,
                                    highlightMode = highlightMode,
                                    primaryMuscle = primaryMuscle,
                                    secondaryMuscles = secondaryMuscles,
                                    intensityByMuscle = intensityByMuscle,
                                    baseFillColor = baseFillColor,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                )
                            // Highlighted muscles get a soft radial gradient (lighter core fading
                            // to the flat fill color) so they read as a rounded, fleshed-out bulge
                            // rather than a flat sticker; neutral/decorative parts stay flat.
                            if (fillColor != baseFillColor) {
                                val bounds = part.path.getBounds()
                                drawPath(
                                    part.path,
                                    brush =
                                        Brush.radialGradient(
                                            colors = listOf(lerp(fillColor, Color.White, 0.25f), fillColor),
                                            center = bounds.center,
                                            radius = maxOf(bounds.width, bounds.height) * 0.75f,
                                        ),
                                )
                            } else {
                                drawPath(part.path, color = fillColor)
                            }
                            drawPath(part.path, color = outlineColor, style = Stroke(width = strokeWidth))
                        }
                    }
                }
            }

            if (interactive && onMuscleSelected != null) {
                parts.forEach { part ->
                    val muscle = part.muscle ?: return@forEach
                    val bounds = part.path.getBounds()
                    val label = stringResource(muscle.labelRes())
                    Box(
                        modifier =
                            Modifier
                                .offset {
                                    IntOffset(
                                        (offsetX + bounds.left * scale).roundToInt(),
                                        (offsetY + bounds.top * scale).roundToInt(),
                                    )
                                }.size(
                                    width = with(density) { (bounds.width * scale).toDp() },
                                    height = with(density) { (bounds.height * scale).toDp() },
                                ).clickable(
                                    onClickLabel = label,
                                    role = Role.Button,
                                ) { onMuscleSelected(muscle) }
                                .semantics { contentDescription = label },
                    )
                }
            }
        }
    }
}

private fun colorForMuscle(
    muscle: MuscleGroup?,
    highlightMode: MuscleHighlightMode,
    primaryMuscle: MuscleGroup?,
    secondaryMuscles: ImmutableList<MuscleGroup>,
    intensityByMuscle: ImmutableMap<MuscleGroup, Float>,
    baseFillColor: Color,
    primaryColor: Color,
    secondaryColor: Color,
): Color {
    if (muscle == null) return baseFillColor
    return when (highlightMode) {
        MuscleHighlightMode.NONE -> baseFillColor
        MuscleHighlightMode.DISCRETE ->
            when {
                muscle == primaryMuscle -> primaryColor
                secondaryMuscles.contains(muscle) -> secondaryColor
                else -> baseFillColor
            }
        MuscleHighlightMode.INTENSITY -> {
            val intensity = (intensityByMuscle[muscle] ?: 0f).coerceIn(0f, 1f)
            lerp(baseFillColor, primaryColor, intensity)
        }
    }
}
