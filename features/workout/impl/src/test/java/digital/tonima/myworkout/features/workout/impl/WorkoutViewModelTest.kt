package digital.tonima.myworkout.features.workout.impl

import digital.tonima.myworkout.data.model.WorkoutEntity
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
            val workout = WorkoutEntity(id = 1, name = "Test")
            viewModel.onIntent(WorkoutIntent.DeleteWorkout(workout))
            coVerify { repository.deleteWorkout(workout) }
        }

    @Test
    fun `startWorkout should call startSession`() =
        runTest {
            coEvery { repository.startSession(any()) } returns 1L
            every { repository.getSessionById(any()) } returns flowOf(null)

            viewModel.onIntent(WorkoutIntent.StartWorkout(1L))

            coVerify { repository.startSession(1L) }
        }
}
