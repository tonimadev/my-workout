package digital.tonima.myworkout.data.catalog

import digital.tonima.myworkout.data.model.MuscleGroup

data class CatalogSetSpec(val targetReps: Int, val restInterval: Int = 60)

data class CatalogExercise(
    val name: String,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val defaultSets: List<CatalogSetSpec> = defaultThreeSets(12),
)

private fun defaultThreeSets(
    reps: Int,
    rest: Int = 60,
): List<CatalogSetSpec> = List(3) { CatalogSetSpec(targetReps = reps, restInterval = rest) }

/**
 * Curated list of common gym exercises, covering every [MuscleGroup], used to speed up manual
 * exercise registration (autocomplete) and to build [WorkoutTemplate]s.
 */
object ExerciseCatalog {
    val all: List<CatalogExercise> =
        listOf(
            CatalogExercise("Supino Reto", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)),
            CatalogExercise("Supino Inclinado", MuscleGroup.CHEST, listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)),
            CatalogExercise("Crucifixo", MuscleGroup.CHEST, listOf(MuscleGroup.SHOULDERS)),
            CatalogExercise("Flexão de Braço", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)),
            CatalogExercise("Puxada Frontal", MuscleGroup.LATS, listOf(MuscleGroup.BICEPS)),
            CatalogExercise("Remada Curvada", MuscleGroup.LATS, listOf(MuscleGroup.BICEPS, MuscleGroup.TRAPEZIUS)),
            CatalogExercise("Remada Baixa", MuscleGroup.LATS, listOf(MuscleGroup.BICEPS, MuscleGroup.TRAPEZIUS)),
            CatalogExercise("Barra Fixa", MuscleGroup.LATS, listOf(MuscleGroup.BICEPS), defaultThreeSets(8)),
            CatalogExercise("Desenvolvimento com Halteres", MuscleGroup.SHOULDERS, listOf(MuscleGroup.TRICEPS)),
            CatalogExercise("Elevação Lateral", MuscleGroup.SHOULDERS),
            CatalogExercise("Elevação Frontal", MuscleGroup.SHOULDERS),
            CatalogExercise("Rosca Direta", MuscleGroup.BICEPS, defaultSets = defaultThreeSets(10)),
            CatalogExercise("Rosca Alternada", MuscleGroup.BICEPS, defaultSets = defaultThreeSets(10)),
            CatalogExercise("Rosca Martelo", MuscleGroup.BICEPS, listOf(MuscleGroup.FOREARMS)),
            CatalogExercise("Tríceps Pulley", MuscleGroup.TRICEPS, defaultSets = defaultThreeSets(12)),
            CatalogExercise("Tríceps Testa", MuscleGroup.TRICEPS, defaultSets = defaultThreeSets(10)),
            CatalogExercise("Mergulho no Banco", MuscleGroup.TRICEPS, listOf(MuscleGroup.CHEST)),
            CatalogExercise("Rosca de Punho", MuscleGroup.FOREARMS, defaultSets = defaultThreeSets(15)),
            CatalogExercise("Abdominal Supra", MuscleGroup.ABS, defaultSets = defaultThreeSets(20)),
            CatalogExercise("Prancha", MuscleGroup.ABS, defaultSets = defaultThreeSets(1, rest = 45)),
            CatalogExercise("Elevação de Pernas", MuscleGroup.ABS, defaultSets = defaultThreeSets(15)),
            CatalogExercise("Encolhimento de Ombros", MuscleGroup.TRAPEZIUS, defaultSets = defaultThreeSets(12)),
            CatalogExercise(
                "Hiperextensão",
                MuscleGroup.LOWER_BACK,
                listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
            ),
            CatalogExercise("Elevação Pélvica", MuscleGroup.GLUTES, listOf(MuscleGroup.HAMSTRINGS)),
            CatalogExercise("Agachamento Sumô", MuscleGroup.GLUTES, listOf(MuscleGroup.QUADRICEPS)),
            CatalogExercise(
                "Agachamento Livre",
                MuscleGroup.QUADRICEPS,
                listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
            ),
            CatalogExercise("Leg Press", MuscleGroup.QUADRICEPS, listOf(MuscleGroup.GLUTES)),
            CatalogExercise("Cadeira Extensora", MuscleGroup.QUADRICEPS, defaultSets = defaultThreeSets(12)),
            CatalogExercise("Avanço", MuscleGroup.QUADRICEPS, listOf(MuscleGroup.GLUTES)),
            CatalogExercise("Stiff", MuscleGroup.HAMSTRINGS, listOf(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK)),
            CatalogExercise("Mesa Flexora", MuscleGroup.HAMSTRINGS, defaultSets = defaultThreeSets(12)),
            CatalogExercise("Elevação de Panturrilha em Pé", MuscleGroup.CALVES, defaultSets = defaultThreeSets(15)),
        )
}
