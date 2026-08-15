package digital.tonima.myworkout.ui.stats

import app.cash.turbine.test
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.repository.GamificationRepository
import digital.tonima.myworkout.data.repository.WorkoutRepository
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {
    private val repository = mockk<WorkoutRepository>(relaxed = true)
    private val gamificationRepository =
        mockk<GamificationRepository>(relaxed = true)
    private lateinit var viewModel: StatsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllMasterExercises() } returns flowOf(emptyList())
        viewModel = StatsViewModel(repository, gamificationRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `masterExercises should emit repository data`() =
        runTest {
            val exercises = listOf(MasterExerciseEntity(id = 1, name = "Squat"))
            every { repository.getAllMasterExercises() } returns flowOf(exercises)

            // Re-create to pick up mocked data
            viewModel = StatsViewModel(repository, gamificationRepository)

            viewModel.masterExercises.test {
                assertEquals(exercises, awaitItem())
            }
        }

    @Test
    fun `selectExercise should update selectedExerciseId`() =
        runTest {
            viewModel.selectedExerciseId.test {
                assertEquals(null, awaitItem())
                viewModel.selectExercise(1L)
                assertEquals(1L, awaitItem())
            }
        }
}
