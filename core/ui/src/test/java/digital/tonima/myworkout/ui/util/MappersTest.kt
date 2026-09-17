package digital.tonima.myworkout.ui.util

import digital.tonima.myworkout.data.model.ExerciseEntity
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.model.MuscleGroup
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {
    @Test
    fun `ExerciseWithSets toUiModel sorts sets by order regardless of input list order`() {
        // Mirrors how sets actually come back from Room's un-ordered @Relation query: list
        // position must never be trusted as display order, only the `order` field.
        val exercise = ExerciseEntity(id = 1L, workoutId = 1L, name = "Squat", order = 0)
        val setOrder2 = SetEntity(id = 10L, exerciseId = 1L, targetWeight = 40.0, targetReps = 10, order = 2)
        val setOrder0 = SetEntity(id = 11L, exerciseId = 1L, targetWeight = 40.0, targetReps = 10, order = 0)
        val setOrder1 = SetEntity(id = 12L, exerciseId = 1L, targetWeight = 40.0, targetReps = 10, order = 1)

        val uiModel = ExerciseWithSets(exercise, listOf(setOrder2, setOrder0, setOrder1)).toUiModel()

        assertEquals(listOf(11L, 12L, 10L), uiModel.sets.map { it.id })
    }

    @Test
    fun `WorkoutWithExercises toUiModel maps nested exercises and sets`() {
        val workout = WorkoutEntity(id = 1L, name = "Push Day", description = "Chest and triceps")
        val exercise = ExerciseEntity(id = 2L, workoutId = 1L, masterExerciseId = 5L, name = "Bench", order = 0)
        val set = SetEntity(id = 3L, exerciseId = 2L, targetWeight = 60.0, targetReps = 8, order = 0)
        val workoutWithExercises = WorkoutWithExercises(workout, listOf(ExerciseWithSets(exercise, listOf(set))))

        val uiModel = workoutWithExercises.toUiModel()

        assertEquals(1L, uiModel.id)
        assertEquals("Push Day", uiModel.name)
        assertEquals(1, uiModel.exercises.size)
        val exerciseUiModel = uiModel.exercises.single()
        assertEquals(5L, exerciseUiModel.masterExerciseId)
        assertEquals(1, exerciseUiModel.sets.size)
        assertEquals(60.0, exerciseUiModel.sets.single().targetWeight, 0.0)
    }

    @Test
    fun `MasterExerciseEntity toUiModel maps a classified primary and secondary muscles`() {
        val entity =
            MasterExerciseEntity(
                id = 1L,
                name = "Supino Reto",
                primaryMuscle = MuscleGroup.CHEST.name,
                secondaryMuscles = "TRICEPS,SHOULDERS",
            )

        val uiModel = entity.toUiModel()

        assertEquals(MuscleGroup.CHEST, uiModel.primaryMuscle)
        assertEquals(listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS), uiModel.secondaryMuscles)
    }

    @Test
    fun `MasterExerciseEntity toUiModel handles an unclassified exercise without crashing`() {
        val entity = MasterExerciseEntity(id = 1L, name = "Novo Exercício", primaryMuscle = null, secondaryMuscles = "")

        val uiModel = entity.toUiModel()

        assertNull(uiModel.primaryMuscle)
        assertTrue(uiModel.secondaryMuscles.isEmpty())
    }
}
