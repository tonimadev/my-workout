package digital.tonima.myworkout.data.repository

import android.util.Log
import digital.tonima.myworkout.data.local.WorkoutDao
import digital.tonima.myworkout.data.local.WorkoutSessionDao
import digital.tonima.myworkout.data.model.ExerciseEntity
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.model.WorkoutSessionEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import digital.tonima.myworkout.data.wearable.WearableSyncManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WorkoutRepositoryTest {
    private val workoutDao = mockk<WorkoutDao>(relaxed = true)
    private val workoutSessionDao = mockk<WorkoutSessionDao>(relaxed = true)
    private val wearableSyncManager = mockk<WearableSyncManager>(relaxed = true)
    private val gamificationRepository = mockk<GamificationRepository>(relaxed = true)
    private lateinit var repository: WorkoutRepository

    @Before
    fun setup() {
        mockkStatic(Log::class)
        repository =
            WorkoutRepositoryImpl(
                workoutDao,
                workoutSessionDao,
                wearableSyncManager,
                gamificationRepository,
            )
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

    @Test
    fun `importWorkout should re-resolve exercises to local master exercises by name`() =
        runTest {
            // The imported workout was exported from a different install: its masterExerciseId
            // (777) is a foreign-key value from that other device's database and must never be
            // trusted as-is, or it will silently point at an unrelated (or nonexistent) exercise
            // once inserted locally. The exercise must be re-resolved locally by name instead.
            val importedExercise =
                ExerciseEntity(id = 99L, workoutId = 5L, masterExerciseId = 777L, name = "Supino Reto", order = 0)
            val importedSet =
                SetEntity(id = 1L, exerciseId = 99L, targetWeight = 40.0, targetReps = 10, order = 0)
            val importedWorkout =
                WorkoutWithExercises(
                    workout = WorkoutEntity(id = 5L, name = "Shared Workout"),
                    exercises = listOf(ExerciseWithSets(importedExercise, listOf(importedSet))),
                )

            val localMasterExercise = MasterExerciseEntity(id = 42L, name = "Supino Reto")
            coEvery { workoutDao.getAllMasterExercises() } returns flowOf(listOf(localMasterExercise))
            coEvery { workoutDao.getAllWorkoutsWithExercisesSync() } returns emptyList()

            repository.importWorkout(importedWorkout)

            coVerify {
                workoutDao.upsertWorkoutWithExercises(
                    match { it.id == 0L && it.name == "Shared Workout" },
                    match { exercises ->
                        exercises.single().exercise.masterExerciseId == localMasterExercise.id &&
                            exercises.single().exercise.id == 0L &&
                            exercises.single().exercise.workoutId == 0L
                    },
                )
            }
        }

    @Test
    fun `finishSession should update session, process gamification and sync to wearable`() =
        runTest {
            val sessionId = 1L
            val session = WorkoutSessionEntity(id = sessionId, workoutId = 1, startTime = 0L)
            val sessionWithLogs = SessionWithLogs(session, null, emptyList())
            coEvery { workoutSessionDao.getSessionWithLogs(sessionId) } returns flowOf(sessionWithLogs)

            repository.finishSession(sessionId)

            coVerify { workoutSessionDao.updateSession(match { it.id == sessionId && it.endTime != null }) }
            coVerify { gamificationRepository.processSessionCompletion(sessionId) }
            coVerify { wearableSyncManager.syncSession(sessionWithLogs) }
        }
}
