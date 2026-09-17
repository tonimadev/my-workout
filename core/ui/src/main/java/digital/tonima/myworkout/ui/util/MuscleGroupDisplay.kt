package digital.tonima.myworkout.ui.util

import androidx.annotation.StringRes
import digital.tonima.myworkout.core.ui.R
import digital.tonima.myworkout.data.model.BodyView
import digital.tonima.myworkout.data.model.MuscleGroup

@StringRes
fun MuscleGroup.labelRes(): Int =
    when (this) {
        MuscleGroup.CHEST -> R.string.muscle_chest
        MuscleGroup.SHOULDERS -> R.string.muscle_shoulders
        MuscleGroup.BICEPS -> R.string.muscle_biceps
        MuscleGroup.TRICEPS -> R.string.muscle_triceps
        MuscleGroup.FOREARMS -> R.string.muscle_forearms
        MuscleGroup.ABS -> R.string.muscle_abs
        MuscleGroup.TRAPEZIUS -> R.string.muscle_trapezius
        MuscleGroup.LATS -> R.string.muscle_lats
        MuscleGroup.LOWER_BACK -> R.string.muscle_lower_back
        MuscleGroup.GLUTES -> R.string.muscle_glutes
        MuscleGroup.QUADRICEPS -> R.string.muscle_quadriceps
        MuscleGroup.HAMSTRINGS -> R.string.muscle_hamstrings
        MuscleGroup.CALVES -> R.string.muscle_calves
    }

@StringRes
fun BodyView.labelRes(): Int =
    when (this) {
        BodyView.FRONT -> R.string.body_view_front
        BodyView.BACK -> R.string.body_view_back
    }
