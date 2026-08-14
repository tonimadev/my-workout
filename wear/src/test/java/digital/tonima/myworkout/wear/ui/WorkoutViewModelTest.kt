package digital.tonima.myworkout.wear.ui

import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.data.util.AlertManager
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
    fun `init should requestSync`() =
        runTest {
            coVerify { repository.requestSync() }
        }

    @Test
    fun `loadWorkout should call repository`() =
        runTest {
            viewModel.loadWorkout(1L)
            coVerify { repository.getWorkoutById(1L) }
        }
}
