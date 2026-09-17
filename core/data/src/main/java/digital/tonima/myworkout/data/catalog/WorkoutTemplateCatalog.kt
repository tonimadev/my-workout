package digital.tonima.myworkout.data.catalog

import digital.tonima.myworkout.data.model.MuscleGroup

data class WorkoutTemplate(
    val name: String,
    val description: String = "",
    val exercises: List<CatalogExercise>,
)

private fun exercise(name: String): CatalogExercise = ExerciseCatalog.all.first { it.name == name }

/**
 * Curated ready-to-use workout templates, so a new user can start training without typing every
 * exercise by hand. Each template pulls its exercises straight from [ExerciseCatalog], so they
 * always come with a [MuscleGroup] already assigned.
 */
object WorkoutTemplateCatalog {
    val all: List<WorkoutTemplate> =
        listOf(
            WorkoutTemplate(
                name = "Full Body",
                description = "Treino único para o corpo inteiro, ideal para iniciantes.",
                exercises =
                    listOf(
                        exercise("Supino Reto"),
                        exercise("Puxada Frontal"),
                        exercise("Agachamento Livre"),
                        exercise("Desenvolvimento com Halteres"),
                        exercise("Rosca Direta"),
                        exercise("Abdominal Supra"),
                    ),
            ),
            WorkoutTemplate(
                name = "Treino A - Peito, Ombro e Tríceps",
                description = "Divisão ABC, dia de empurrar.",
                exercises =
                    listOf(
                        exercise("Supino Reto"),
                        exercise("Supino Inclinado"),
                        exercise("Desenvolvimento com Halteres"),
                        exercise("Elevação Lateral"),
                        exercise("Tríceps Pulley"),
                        exercise("Tríceps Testa"),
                    ),
            ),
            WorkoutTemplate(
                name = "Treino B - Costas e Bíceps",
                description = "Divisão ABC, dia de puxar.",
                exercises =
                    listOf(
                        exercise("Puxada Frontal"),
                        exercise("Remada Curvada"),
                        exercise("Remada Baixa"),
                        exercise("Rosca Direta"),
                        exercise("Rosca Martelo"),
                    ),
            ),
            WorkoutTemplate(
                name = "Treino C - Pernas e Abdômen",
                description = "Divisão ABC, dia de pernas.",
                exercises =
                    listOf(
                        exercise("Agachamento Livre"),
                        exercise("Leg Press"),
                        exercise("Cadeira Extensora"),
                        exercise("Stiff"),
                        exercise("Elevação de Panturrilha em Pé"),
                        exercise("Abdominal Supra"),
                    ),
            ),
            WorkoutTemplate(
                name = "Push - Peito, Ombro e Tríceps",
                description = "Divisão Push/Pull/Legs, dia de empurrar.",
                exercises =
                    listOf(
                        exercise("Supino Reto"),
                        exercise("Crucifixo"),
                        exercise("Elevação Lateral"),
                        exercise("Mergulho no Banco"),
                    ),
            ),
            WorkoutTemplate(
                name = "Pull - Costas e Bíceps",
                description = "Divisão Push/Pull/Legs, dia de puxar.",
                exercises =
                    listOf(
                        exercise("Barra Fixa"),
                        exercise("Remada Curvada"),
                        exercise("Rosca Alternada"),
                        exercise("Encolhimento de Ombros"),
                    ),
            ),
            WorkoutTemplate(
                name = "Legs - Pernas e Glúteos",
                description = "Divisão Push/Pull/Legs, dia de pernas.",
                exercises =
                    listOf(
                        exercise("Agachamento Sumô"),
                        exercise("Avanço"),
                        exercise("Mesa Flexora"),
                        exercise("Elevação Pélvica"),
                        exercise("Elevação de Panturrilha em Pé"),
                    ),
            ),
        )
}
