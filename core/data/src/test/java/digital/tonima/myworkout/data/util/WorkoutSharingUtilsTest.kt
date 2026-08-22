package digital.tonima.myworkout.data.util

import digital.tonima.myworkout.data.model.ExerciseEntity
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import digital.tonima.myworkout.data.util.WorkoutSharingUtils.toJson
import digital.tonima.myworkout.data.util.WorkoutSharingUtils.toShareableText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WorkoutSharingUtilsTest {
    @Test
    fun `toShareableText should format correctly`() {
        val workout =
            WorkoutWithExercises(
                workout = WorkoutEntity(name = "Test Workout", description = "Test Desc"),
                exercises =
                    listOf(
                        ExerciseWithSets(
                            exercise = ExerciseEntity(name = "Pushups", order = 0, workoutId = 0),
                            sets =
                                listOf(
                                    SetEntity(targetWeight = 0.0, targetReps = 10, exerciseId = 0, order = 0),
                                ),
                        ),
                    ),
            )

        val text = workout.toShareableText()
        assert(text.contains("Test Workout"))
        assert(text.contains("Pushups"))
        assert(text.contains("0.0kg x 10"))
    }

    @Test
    fun `toJson and decodeJsonToWorkout should be consistent`() {
        val original =
            WorkoutWithExercises(
                workout = WorkoutEntity(name = "Test Workout", description = "Test Desc"),
                exercises =
                    listOf(
                        ExerciseWithSets(
                            exercise = ExerciseEntity(name = "Pushups", order = 0, workoutId = 0),
                            sets =
                                listOf(
                                    SetEntity(targetWeight = 0.0, targetReps = 10, exerciseId = 0, order = 0),
                                ),
                        ),
                    ),
            )

        val json = original.toJson()
        val decoded = WorkoutSharingUtils.decodeJsonToWorkout(json)

        assertNotNull(decoded)
        assertEquals(original.workout.name, decoded!!.workout.name)
        assertEquals(original.exercises.size, decoded.exercises.size)
        assertEquals(original.exercises[0].exercise.name, decoded.exercises[0].exercise.name)
    }
}
