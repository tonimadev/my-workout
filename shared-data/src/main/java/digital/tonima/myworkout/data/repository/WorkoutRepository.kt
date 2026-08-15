package digital.tonima.myworkout.data.repository

import android.util.Log
import digital.tonima.myworkout.data.local.WorkoutDao
import digital.tonima.myworkout.data.local.WorkoutSessionDao
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.SyncData
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.model.WorkoutSessionEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import digital.tonima.myworkout.data.wearable.WearableSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<WorkoutWithExercises>>

    fun getWorkoutById(id: Long): Flow<WorkoutWithExercises?>

    suspend fun addWorkout(
        workout: WorkoutEntity,
        exercises: List<ExerciseWithSets>,
    )

    suspend fun deleteWorkout(workout: WorkoutEntity)

    fun getAllMasterExercises(): Flow<List<MasterExerciseEntity>>

    suspend fun addMasterExercise(
        name: String,
        description: String = "",
    ): Long

    suspend fun upsertMasterExercise(masterExercise: MasterExerciseEntity)

    fun getLogsForMasterExercise(masterExerciseId: Long): Flow<List<WorkoutLogEntity>>

    fun getAllSessions(): Flow<List<SessionWithLogs>>

    fun getSessionById(id: Long): Flow<SessionWithLogs?>

    suspend fun startSession(workoutId: Long): Long

    suspend fun finishSession(session: WorkoutSessionEntity)

    suspend fun addLog(log: WorkoutLogEntity)

    suspend fun requestSync()

    suspend fun forceSync()
}

@Singleton
class WorkoutRepositoryImpl
    @Inject
    constructor(
        private val workoutDao: WorkoutDao,
        private val workoutSessionDao: WorkoutSessionDao,
        private val wearableSyncManager: WearableSyncManager,
        private val gamificationRepository: GamificationRepository,
    ) : WorkoutRepository {
        override fun getAllWorkouts(): Flow<List<WorkoutWithExercises>> = workoutDao.getAllWorkoutsWithExercises()

        override fun getWorkoutById(id: Long): Flow<WorkoutWithExercises?> = workoutDao.getWorkoutWithExercises(id)

        override suspend fun addWorkout(
            workout: WorkoutEntity,
            exercises: List<ExerciseWithSets>,
        ) {
            workoutDao.upsertWorkoutWithExercises(workout, exercises)
            syncWorkoutsToWearable()
        }

        private suspend fun syncWorkoutsToWearable() {
            try {
                val workouts = workoutDao.getAllWorkoutsWithExercisesSync()
                val masters = workoutDao.getAllMasterExercises().first()
                wearableSyncManager.syncWorkouts(SyncData(workouts, masters))
            } catch (e: Exception) {
                Log.e("WorkoutRepository", "Error syncing workouts to wearable", e)
            }
        }

        override suspend fun deleteWorkout(workout: WorkoutEntity) {
            workoutDao.deleteWorkout(workout)
            syncWorkoutsToWearable()
        }

        override fun getAllMasterExercises(): Flow<List<MasterExerciseEntity>> = workoutDao.getAllMasterExercises()

        override suspend fun addMasterExercise(
            name: String,
            description: String,
        ): Long {
            return workoutDao.insertMasterExercise(MasterExerciseEntity(name = name, description = description))
        }

        override suspend fun upsertMasterExercise(masterExercise: MasterExerciseEntity) {
            workoutDao.insertMasterExercise(masterExercise)
        }

        override fun getLogsForMasterExercise(masterExerciseId: Long): Flow<List<WorkoutLogEntity>> =
            workoutSessionDao.getLogsForMasterExercise(masterExerciseId)

        override fun getAllSessions(): Flow<List<SessionWithLogs>> = workoutSessionDao.getAllSessionsWithLogs()

        override fun getSessionById(id: Long): Flow<SessionWithLogs?> = workoutSessionDao.getSessionWithLogs(id)

        override suspend fun startSession(workoutId: Long): Long {
            val session =
                WorkoutSessionEntity(
                    workoutId = workoutId,
                    startTime = System.currentTimeMillis(),
                )
            return workoutSessionDao.insertSession(session)
        }

        override suspend fun finishSession(session: WorkoutSessionEntity) {
            workoutSessionDao.updateSession(session.copy(endTime = System.currentTimeMillis()))
            gamificationRepository.processSessionCompletion(session.id)
            wearableSyncManager.syncFinishSession(session.id)
        }

        override suspend fun addLog(log: WorkoutLogEntity) {
            workoutSessionDao.insertLog(log)
            wearableSyncManager.syncLog(log)
        }

        override suspend fun forceSync() {
            syncWorkoutsToWearable()
        }

        override suspend fun requestSync() {
            wearableSyncManager.sendMessage("/workout/request_sync", byteArrayOf())
        }
    }
