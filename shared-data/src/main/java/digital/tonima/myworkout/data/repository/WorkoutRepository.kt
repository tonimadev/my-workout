package digital.tonima.myworkout.data.repository

import digital.tonima.myworkout.data.local.WorkoutDao
import digital.tonima.myworkout.data.local.WorkoutSessionDao
import digital.tonima.myworkout.data.model.*
import digital.tonima.myworkout.data.wearable.WearableSyncManager
import kotlinx.coroutines.flow.Flow
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

    fun getLogsForMasterExercise(masterExerciseId: Long): Flow<List<WorkoutLogEntity>>

    fun getAllSessions(): Flow<List<SessionWithLogs>>

    fun getSessionById(id: Long): Flow<SessionWithLogs?>

    suspend fun startSession(workoutId: Long): Long

    suspend fun finishSession(session: WorkoutSessionEntity)

    suspend fun addLog(log: WorkoutLogEntity)
}

@Singleton
class WorkoutRepositoryImpl
    @Inject
    constructor(
        private val workoutDao: WorkoutDao,
        private val workoutSessionDao: WorkoutSessionDao,
        private val wearableSyncManager: WearableSyncManager,
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
                wearableSyncManager.syncWorkouts(workouts)
            } catch (e: Exception) {
                e.printStackTrace()
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
        }

        override suspend fun addLog(log: WorkoutLogEntity) {
            workoutSessionDao.insertLog(log)
            wearableSyncManager.syncLog(log)
        }
    }
