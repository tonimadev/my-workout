package digital.tonima.myworkout.data.model

import kotlinx.serialization.Serializable

enum class BodyView { FRONT, BACK }

@Serializable
enum class MuscleGroup(val view: BodyView) {
    CHEST(BodyView.FRONT),
    SHOULDERS(BodyView.FRONT),
    BICEPS(BodyView.FRONT),
    TRICEPS(BodyView.BACK),
    FOREARMS(BodyView.FRONT),
    ABS(BodyView.FRONT),
    TRAPEZIUS(BodyView.BACK),
    LATS(BodyView.BACK),
    LOWER_BACK(BodyView.BACK),
    GLUTES(BodyView.BACK),
    QUADRICEPS(BodyView.FRONT),
    HAMSTRINGS(BodyView.BACK),
    CALVES(BodyView.BACK),
}

private const val SECONDARY_MUSCLE_SEPARATOR = ","

fun MasterExerciseEntity.primaryMuscleGroup(): MuscleGroup? =
    primaryMuscle?.let { raw -> runCatching { MuscleGroup.valueOf(raw) }.getOrNull() }

fun MasterExerciseEntity.secondaryMuscleGroups(): List<MuscleGroup> =
    secondaryMuscles.split(SECONDARY_MUSCLE_SEPARATOR)
        .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
        .mapNotNull { raw -> runCatching { MuscleGroup.valueOf(raw) }.getOrNull() }

fun MuscleGroup?.toEntityPrimaryMuscle(): String? = this?.name

fun List<MuscleGroup>.toEntitySecondaryMuscles(): String = joinToString(SECONDARY_MUSCLE_SEPARATOR) { it.name }
