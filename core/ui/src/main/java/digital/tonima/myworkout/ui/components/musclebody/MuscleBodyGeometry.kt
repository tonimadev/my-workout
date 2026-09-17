package digital.tonima.myworkout.ui.components.musclebody

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import digital.tonima.myworkout.data.model.BodyView
import digital.tonima.myworkout.data.model.MuscleGroup

/** Normalized coordinate space the body is drawn in before being scaled to the real canvas size. */
const val BODY_WIDTH = 100f
const val BODY_HEIGHT = 200f
private const val CENTER_X = BODY_WIDTH / 2f

/** One drawable/tappable region of the body diagram. [muscle] is null for decorative-only parts. */
data class BodyPart(val muscle: MuscleGroup?, val path: Path)

private fun oval(
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float = rx,
): Path = Path().apply { addOval(Rect(cx - rx, cy - ry, cx + rx, cy + ry)) }

private fun roundedRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    corner: Float,
): Path =
    Path().apply {
        addRoundRect(RoundRect(Rect(left, top, right, bottom), CornerRadius(corner, corner)))
    }

/** Mirrors a path horizontally around the body's vertical center, to derive a right limb from a
 * hand-authored left one without duplicating (and risking desyncing) every control point. */
private fun Path.mirroredX(): Path {
    val mirrored = android.graphics.Path(asAndroidPath())
    mirrored.transform(
        android.graphics.Matrix().apply {
            setScale(-1f, 1f)
            postTranslate(2 * CENTER_X, 0f)
        },
    )
    return mirrored.asComposePath()
}

// --- Head / neck -----------------------------------------------------------------------------

private val headPath = oval(cx = CENTER_X, cy = 13f, rx = 8.5f, ry = 10f)
private val neckPath =
    Path().apply {
        moveTo(45f, 21f)
        lineTo(55f, 21f)
        lineTo(57f, 29f)
        lineTo(43f, 29f)
        close()
    }

// --- Torso backdrop (shared silhouette behind both front and back overlays) ------------------

private val torsoOutlinePath =
    Path().apply {
        moveTo(30f, 31f)
        quadraticTo(50f, 25f, 70f, 31f) // shoulder line, slight trapezius rise at center
        quadraticTo(80f, 55f, 64f, 92f) // chest bulge tapering to waist
        quadraticTo(72f, 108f, 66f, 120f) // waist flaring to hip
        lineTo(34f, 120f)
        quadraticTo(28f, 108f, 36f, 92f)
        quadraticTo(20f, 55f, 30f, 31f)
        close()
    }

// --- Chest / abs (front) -----------------------------------------------------------------------

private val chestLeftPath = oval(cx = 38f, cy = 44f, rx = 12f, ry = 14f)
private val chestRightPath = oval(cx = 62f, cy = 44f, rx = 12f, ry = 14f)
private val absPath = roundedRect(40f, 60f, 60f, 92f, corner = 14f)

// --- Shoulders ---------------------------------------------------------------------------------

private val shoulderLeftPath = oval(cx = 26f, cy = 33f, rx = 10f, ry = 9f)
private val shoulderRightPath = oval(cx = 74f, cy = 33f, rx = 10f, ry = 9f)

// --- Arms (bicep/tricep peak + tapered forearm, hand-authored left, mirrored right) ------------

private val upperArmLeftPath =
    Path().apply {
        moveTo(20f, 36f) // shoulder-outer (under deltoid)
        lineTo(30f, 36f) // shoulder-inner (armpit)
        quadraticTo(27f, 55f, 27f, 71f) // inner edge, fairly straight to elbow
        lineTo(20f, 71f) // elbow (flat bottom edge)
        quadraticTo(13f, 55f, 20f, 36f) // outer edge, bicep-peak bulge
        close()
    }
private val upperArmRightPath = upperArmLeftPath.mirroredX()

private val forearmLeftPath =
    Path().apply {
        moveTo(20f, 71f) // elbow-outer
        lineTo(28f, 71f) // elbow-inner
        quadraticTo(25f, 88f, 25f, 103f) // inner edge to wrist
        lineTo(19f, 103f) // wrist (flat bottom edge)
        quadraticTo(14f, 88f, 20f, 71f) // outer edge, forearm-mass bulge
        close()
    }
private val forearmRightPath = forearmLeftPath.mirroredX()

// --- Back: trapezius / lats / lower back --------------------------------------------------------

private val trapeziusPath =
    Path().apply {
        moveTo(50f, 23f)
        quadraticTo(32f, 28f, 28f, 42f)
        lineTo(72f, 42f)
        quadraticTo(68f, 28f, 50f, 23f)
        close()
    }
private val latsPath =
    Path().apply {
        moveTo(28f, 44f)
        quadraticTo(50f, 50f, 72f, 44f) // dips under the traps at center
        quadraticTo(78f, 64f, 66f, 86f) // right flare tapering to waist
        lineTo(34f, 86f)
        quadraticTo(22f, 64f, 28f, 44f) // left flare mirrored
        close()
    }
private val lowerBackPath = oval(cx = CENTER_X, cy = 95f, rx = 11f, ry = 9f)

// --- Pelvis / glutes (same two-lobe shape; neutral on front, highlighted on back) --------------

private val pelvisLeftPath = oval(cx = 40f, cy = 112f, rx = 13f)
private val pelvisRightPath = oval(cx = 60f, cy = 112f, rx = 13f)

// --- Legs (thigh with quad/hamstring sweep + calf bulge, mirrored right) -----------------------

private val thighLeftPath =
    Path().apply {
        moveTo(35f, 118f) // hip-outer
        lineTo(49f, 118f) // hip-inner (near crotch)
        quadraticTo(45f, 145f, 44f, 163f) // inner edge, gentle taper to knee
        lineTo(35f, 163f) // knee (flat bottom edge)
        quadraticTo(29f, 145f, 35f, 118f) // outer edge, quad-sweep bulge
        close()
    }
private val thighRightPath = thighLeftPath.mirroredX()

private val calfLeftPath =
    Path().apply {
        moveTo(35f, 163f) // knee-outer
        lineTo(44f, 163f) // knee-inner
        quadraticTo(41f, 182f, 40f, 198f) // inner edge to ankle
        lineTo(34f, 198f) // ankle (flat bottom edge)
        quadraticTo(28f, 182f, 35f, 163f) // outer edge, calf-belly bulge
        close()
    }
private val calfRightPath = calfLeftPath.mirroredX()

/** Decorative (non-tappable, never highlighted) parts, drawn first so muscles layer on top. */
private val decorativeParts =
    listOf(
        BodyPart(null, torsoOutlinePath),
        BodyPart(null, headPath),
        BodyPart(null, neckPath),
    )

fun bodyPartsFor(view: BodyView): List<BodyPart> =
    when (view) {
        BodyView.FRONT ->
            decorativeParts +
                listOf(
                    BodyPart(null, pelvisLeftPath),
                    BodyPart(null, pelvisRightPath),
                    BodyPart(MuscleGroup.SHOULDERS, shoulderLeftPath),
                    BodyPart(MuscleGroup.SHOULDERS, shoulderRightPath),
                    BodyPart(MuscleGroup.CHEST, chestLeftPath),
                    BodyPart(MuscleGroup.CHEST, chestRightPath),
                    BodyPart(MuscleGroup.ABS, absPath),
                    BodyPart(MuscleGroup.BICEPS, upperArmLeftPath),
                    BodyPart(MuscleGroup.BICEPS, upperArmRightPath),
                    BodyPart(MuscleGroup.FOREARMS, forearmLeftPath),
                    BodyPart(MuscleGroup.FOREARMS, forearmRightPath),
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
                    BodyPart(MuscleGroup.GLUTES, pelvisLeftPath),
                    BodyPart(MuscleGroup.GLUTES, pelvisRightPath),
                    BodyPart(MuscleGroup.HAMSTRINGS, thighLeftPath),
                    BodyPart(MuscleGroup.HAMSTRINGS, thighRightPath),
                    BodyPart(MuscleGroup.CALVES, calfLeftPath),
                    BodyPart(MuscleGroup.CALVES, calfRightPath),
                )
    }
