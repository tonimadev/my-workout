package digital.tonima.myworkout.ui.components.musclebody

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import digital.tonima.myworkout.data.model.BodyView
import digital.tonima.myworkout.data.model.MuscleGroup

/** Normalized coordinate space the body is drawn in before being scaled to the real canvas size. */
const val BODY_WIDTH = 100f
const val BODY_HEIGHT = 200f

/** One drawable/tappable region of the body diagram. [muscle] is null for decorative-only parts. */
data class BodyPart(val muscle: MuscleGroup?, val path: Path)

private fun roundedRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    corner: Float = 4f,
): Path =
    Path().apply {
        addRoundRect(RoundRect(Rect(left, top, right, bottom), CornerRadius(corner, corner)))
    }

private fun circle(
    cx: Float,
    cy: Float,
    r: Float,
): Path = Path().apply { addOval(Rect(cx - r, cy - r, cx + r, cy + r)) }

private fun polygon(points: List<Offset>): Path =
    Path().apply {
        moveTo(points[0].x, points[0].y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }

private val headPath = circle(cx = 50f, cy = 12f, r = 9f)
private val neckPath = roundedRect(44f, 21f, 56f, 28f)
private val shoulderLeftPath = circle(cx = 26f, cy = 32f, r = 8f)
private val shoulderRightPath = circle(cx = 74f, cy = 32f, r = 8f)
private val chestPath = roundedRect(34f, 30f, 66f, 54f, corner = 8f)
private val absPath = roundedRect(38f, 54f, 62f, 88f, corner = 6f)
private val upperArmLeftPath = roundedRect(18f, 34f, 32f, 70f, corner = 6f)
private val upperArmRightPath = roundedRect(68f, 34f, 82f, 70f, corner = 6f)
private val forearmLeftPath = roundedRect(18f, 70f, 30f, 100f, corner = 6f)
private val forearmRightPath = roundedRect(70f, 70f, 82f, 100f, corner = 6f)
private val trapeziusPath = polygon(listOf(Offset(50f, 24f), Offset(28f, 42f), Offset(72f, 42f)))
private val latsPath =
    polygon(listOf(Offset(30f, 42f), Offset(70f, 42f), Offset(60f, 82f), Offset(40f, 82f)))
private val lowerBackPath = roundedRect(40f, 82f, 60f, 92f, corner = 4f)
private val pelvisGlutesPath = roundedRect(36f, 88f, 64f, 118f, corner = 8f)
private val thighLeftPath = roundedRect(34f, 118f, 48f, 162f, corner = 6f)
private val thighRightPath = roundedRect(52f, 118f, 66f, 162f, corner = 6f)
private val calfLeftPath = roundedRect(34f, 162f, 48f, 196f, corner = 6f)
private val calfRightPath = roundedRect(52f, 162f, 66f, 196f, corner = 6f)

/** All decorative (non-tappable, never highlighted) parts, shared by both views. */
private val decorativeParts = listOf(BodyPart(null, headPath), BodyPart(null, neckPath))

fun bodyPartsFor(view: BodyView): List<BodyPart> =
    when (view) {
        BodyView.FRONT ->
            decorativeParts +
                listOf(
                    BodyPart(MuscleGroup.SHOULDERS, shoulderLeftPath),
                    BodyPart(MuscleGroup.SHOULDERS, shoulderRightPath),
                    BodyPart(MuscleGroup.CHEST, chestPath),
                    BodyPart(MuscleGroup.ABS, absPath),
                    BodyPart(MuscleGroup.BICEPS, upperArmLeftPath),
                    BodyPart(MuscleGroup.BICEPS, upperArmRightPath),
                    BodyPart(MuscleGroup.FOREARMS, forearmLeftPath),
                    BodyPart(MuscleGroup.FOREARMS, forearmRightPath),
                    BodyPart(null, pelvisGlutesPath),
                    BodyPart(MuscleGroup.QUADRICEPS, thighLeftPath),
                    BodyPart(MuscleGroup.QUADRICEPS, thighRightPath),
                    BodyPart(null, calfLeftPath),
                    BodyPart(null, calfRightPath),
                )
        BodyView.BACK ->
            decorativeParts +
                listOf(
                    BodyPart(null, shoulderLeftPath),
                    BodyPart(null, shoulderRightPath),
                    BodyPart(MuscleGroup.TRAPEZIUS, trapeziusPath),
                    BodyPart(MuscleGroup.LATS, latsPath),
                    BodyPart(MuscleGroup.LOWER_BACK, lowerBackPath),
                    BodyPart(MuscleGroup.TRICEPS, upperArmLeftPath),
                    BodyPart(MuscleGroup.TRICEPS, upperArmRightPath),
                    BodyPart(null, forearmLeftPath),
                    BodyPart(null, forearmRightPath),
                    BodyPart(MuscleGroup.GLUTES, pelvisGlutesPath),
                    BodyPart(MuscleGroup.HAMSTRINGS, thighLeftPath),
                    BodyPart(MuscleGroup.HAMSTRINGS, thighRightPath),
                    BodyPart(MuscleGroup.CALVES, calfLeftPath),
                    BodyPart(MuscleGroup.CALVES, calfRightPath),
                )
    }
