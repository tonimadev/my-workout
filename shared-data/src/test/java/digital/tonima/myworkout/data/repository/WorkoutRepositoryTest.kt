package digital.tonima.myworkout.data.repository

import digital.tonima.myworkout.data.local.WorkoutDao
import digital.tonima.myworkout.data.local.WorkoutSessionDao
import digital.tonima.myworkout.data.model.*
import digital.tonima.myworkout.data.wearable.WearableSyncManager
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WorkoutRepositoryTest {
    private val workoutDao = mockk<WorkoutDao>(relaxed = true)
    private val workoutSessionDao = mockk<WorkoutSessionDao>(relaxed = true)
    private val wearableSyncManager = mockk<WearableSyncManager>(relaxed = true)
    private lateinit var repository: WorkoutRepository

    @Before
    fun setup() {
        repository = WorkoutRepositoryImpl(workoutDao, workoutSessionDao, wearableSyncManager)
    }

    @Test
    fun `addWorkout should upsert and sync to wearable`() =
        runTest {
            val workout = WorkoutEntity(name = "Test")
            val exercises = listOf<ExerciseWithSets>()

            coEvery { workoutDao.getAllWorkoutsWithExercisesSync() } returns emptyList()
            coEvery { workoutDao.getAllMasterExercises() } returns flowOf(emptyList())

            repository.addWorkout(workout, exercises)

            coVerify { workoutDao.upsertWorkoutWithExercises(workout, exercises) }
            coVerify { wearableSyncManager.syncWorkouts(any()) }
        }

    @Test
    fun `startSession should insert session and return id`() =
        runTest {
            val workoutId = 1L
            coEvery { workoutSessionDao.insertSession(any()) } returns 10L

            val id = repository.startSession(workoutId)

            assert(id == 10L)
            coVerify { workoutSessionDao.insertSession(match { it.workoutId == workoutId }) }
        }

    @Test
    fun `addLog should insert log and sync to wearable`() =
        runTest {
            val log =
                WorkoutLogEntity(
                    sessionId = 1,
                    masterExerciseId = 2,
                    setId = 3,
                    actualWeight = 10.0,
                    actualReps = 10,
                    timestamp = 0L,
                )

            repository.addLog(log)

            coVerify { workoutSessionDao.insertLog(log) }
            coVerify { wearableSyncManager.syncLog(log) }
        }
}
