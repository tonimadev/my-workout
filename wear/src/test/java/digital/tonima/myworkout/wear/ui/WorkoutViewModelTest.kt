package digital.tonima.myworkout.wear.ui

import android.content.Context
import digital.tonima.myworkout.data.model.ExerciseEntity
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutSessionEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import digital.tonima.myworkout.data.repository.WorkoutRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {
    private val repository = mockk<WorkoutRepository>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private lateinit var viewModel: WorkoutViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllWorkouts() } returns flowOf(emptyList())
        viewModel = WorkoutViewModel(repository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should requestSync`() =
        runTest {
            coVerify { repository.requestSync() }
        }

    @Test
    fun `loadWorkout should call repository`() =
        runTest {
            viewModel.onIntent(WorkoutIntent.LoadWorkout(1L))
            coVerify { repository.getWorkoutById(1L) }
        }

    @Test
    fun `completeSet should propagate weight to sets ordered after the target by 'order', not by list position`() =
        runTest {
            val workoutId = 1L
            val exerciseId = 2L
            val newWeight = 55f
            val reps = 8
            val rest = 0

            val workout = WorkoutEntity(id = workoutId, name = "Test Workout")
            val exercise = ExerciseEntity(id = exerciseId, workoutId = workoutId, name = "Squat", order = 0)

            // Sets are returned out of `order` sequence (id 10 has the highest `order` but is
            // first in the list) to mirror Room's @Relation query, which has no ORDER BY and does
            // not guarantee list position matches the `order` field.
            val setOrder2 =
                SetEntity(id = 10L, exerciseId = exerciseId, targetWeight = 40.0, targetReps = 10, order = 2)
            val setOrder0 =
                SetEntity(id = 11L, exerciseId = exerciseId, targetWeight = 40.0, targetReps = 10, order = 0)
            val setOrder1 =
                SetEntity(id = 12L, exerciseId = exerciseId, targetWeight = 40.0, targetReps = 10, order = 1)
            val exerciseWithSets = ExerciseWithSets(exercise, listOf(setOrder2, setOrder0, setOrder1))
            val workoutWithEx = WorkoutWithExercises(workout, listOf(exerciseWithSets))

            val session = WorkoutSessionEntity(id = 10L, workoutId = workoutId, startTime = 0L)
            val sessionWithLogs = SessionWithLogs(session, workout, emptyList())

            every { repository.getActiveSession() } returns flowOf(sessionWithLogs)
            every { repository.getWorkoutById(workoutId) } returns flowOf(workoutWithEx)

            viewModel = WorkoutViewModel(repository, context)

            viewModel.onIntent(
                WorkoutIntent.CompleteSet(exerciseId, setOrder0.id, newWeight, reps, rest),
            )

            coVerify(timeout = 2000) {
                repository.addWorkout(
                    eq(workout),
                    match { exercises ->
                        val sets = exercises.single().sets.associateBy { it.id }
                        sets.getValue(setOrder0.id).targetWeight == newWeight.toDouble() &&
                            sets.getValue(setOrder1.id).targetWeight == newWeight.toDouble() &&
                            sets.getValue(setOrder2.id).targetWeight == newWeight.toDouble()
                    },
                )
            }
        }
}
