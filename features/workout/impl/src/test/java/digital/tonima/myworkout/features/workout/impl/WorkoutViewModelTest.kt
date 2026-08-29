package digital.tonima.myworkout.features.workout.impl

import digital.tonima.myworkout.data.model.ExerciseEntity
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutSessionEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.data.util.AlertManager
import io.mockk.coEvery
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
    private val alertManager = mockk<AlertManager>(relaxed = true)
    private lateinit var viewModel: WorkoutViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllWorkouts() } returns flowOf(emptyList())
        viewModel = WorkoutViewModel(repository, alertManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addWorkout should call repository`() =
        runTest {
            viewModel.onIntent(WorkoutIntent.AddWorkout("New Workout"))
            coVerify { repository.addWorkout(match { it.name == "New Workout" }, any()) }
        }

    @Test
    fun `deleteWorkout should call repository`() =
        runTest {
            val workoutId = 1L
            val workoutEntity = WorkoutEntity(id = workoutId, name = "Test")
            val workoutWithEx = WorkoutWithExercises(workoutEntity, emptyList())
            coEvery { repository.getWorkoutById(workoutId) } returns flowOf(workoutWithEx)

            viewModel.onIntent(WorkoutIntent.DeleteWorkout(workoutId))

            coVerify { repository.deleteWorkout(workoutEntity) }
        }

    @Test
    fun `startWorkout should call startSession`() =
        runTest {
            coEvery { repository.startSession(any()) } returns 1L
            every { repository.getSessionById(any()) } returns flowOf(null)

            viewModel.onIntent(WorkoutIntent.StartWorkout(1L))

            coVerify { repository.startSession(1L) }
        }

    @Test
    fun `logSet should update workout template weight if it changed`() =
        runTest {
            val workoutId = 1L
            val exerciseId = 2L
            val setId = 3L
            val newWeight = 50.0
            val reps = 10
            val rest = 60

            val workout = WorkoutEntity(id = workoutId, name = "Test Workout")
            val exercise = ExerciseEntity(id = exerciseId, workoutId = workoutId, name = "Squat", order = 0)
            val set =
                SetEntity(
                    id = setId,
                    exerciseId = exerciseId,
                    targetWeight = 40.0,
                    targetReps = 10,
                    restInterval = rest,
                    order = 0,
                )
            val exerciseWithSets = ExerciseWithSets(exercise, listOf(set))
            val workoutWithEx = WorkoutWithExercises(workout, listOf(exerciseWithSets))

            val session = WorkoutSessionEntity(id = 10L, workoutId = workoutId, startTime = 0L)
            val sessionWithLogs = SessionWithLogs(session, workout, emptyList())

            coEvery { repository.getWorkoutById(workoutId) } returns flowOf(workoutWithEx)
            coEvery { repository.startSession(workoutId) } returns 10L
            coEvery { repository.getSessionById(10L) } returns flowOf(sessionWithLogs)

            // Start workout to set the state
            viewModel.onIntent(WorkoutIntent.StartWorkout(workoutId))

            // Log set with new weight
            viewModel.onIntent(WorkoutIntent.LogSet(10L, exerciseId, setId, newWeight, reps, rest))

            // Verify that addWorkout was called with the updated weight in the template
            coVerify(timeout = 2000) {
                repository.addWorkout(
                    eq(workout),
                    match { exercises ->
                        exercises.any { ex ->
                            ex.sets.any { s -> s.id == setId && s.targetWeight == newWeight }
                        }
                    },
                )
            }
        }
}
