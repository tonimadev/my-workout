package digital.tonima.myworkout.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MuscleGroupTest {
    @Test
    fun `primaryMuscleGroup returns null when not yet classified`() {
        val entity = MasterExerciseEntity(name = "Novo Exercício", primaryMuscle = null)

        assertNull(entity.primaryMuscleGroup())
    }

    @Test
    fun `primaryMuscleGroup returns null for a stored value that is no longer a valid enum name`() {
        // Guards a real migration hazard: if a MuscleGroup enum constant is ever renamed, rows
        // that stored the old name under the previous version must degrade to "unclassified"
        // instead of crashing the whole app on read.
        val entity = MasterExerciseEntity(name = "Old Exercise", primaryMuscle = "NONEXISTENT_MUSCLE")

        assertNull(entity.primaryMuscleGroup())
    }

    @Test
    fun `secondaryMuscleGroups parses a comma separated list`() {
        val entity =
            MasterExerciseEntity(name = "Supino Reto", secondaryMuscles = "TRICEPS,SHOULDERS")

        assertEquals(listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS), entity.secondaryMuscleGroups())
    }

    @Test
    fun `secondaryMuscleGroups returns an empty list for a blank string`() {
        val entity = MasterExerciseEntity(name = "Novo Exercício", secondaryMuscles = "")

        assertTrue(entity.secondaryMuscleGroups().isEmpty())
    }

    @Test
    fun `secondaryMuscleGroups trims whitespace and drops unknown entries`() {
        val entity = MasterExerciseEntity(name = "Supino Reto", secondaryMuscles = " TRICEPS , BOGUS, SHOULDERS ")

        assertEquals(listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS), entity.secondaryMuscleGroups())
    }

    @Test
    fun `toEntityPrimaryMuscle and toEntitySecondaryMuscles round-trip through primaryMuscleGroup and secondaryMuscleGroups`() {
        val primary: MuscleGroup? = MuscleGroup.CHEST
        val secondary = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)

        val entity =
            MasterExerciseEntity(
                name = "Supino Reto",
                primaryMuscle = primary.toEntityPrimaryMuscle(),
                secondaryMuscles = secondary.toEntitySecondaryMuscles(),
            )

        assertEquals(primary, entity.primaryMuscleGroup())
        assertEquals(secondary, entity.secondaryMuscleGroups())
    }

    @Test
    fun `toEntityPrimaryMuscle of a null muscle stays null`() {
        val primary: MuscleGroup? = null

        assertNull(primary.toEntityPrimaryMuscle())
    }
}
