package digital.tonima.myworkout.data.local

import androidx.room.*
import digital.tonima.myworkout.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterExercise(exercise: MasterExerciseEntity): Long

    @Query("SELECT * FROM master_exercises")
    fun getAllMasterExercises(): Flow<List<MasterExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetEntity): Long

    @Transaction
    @Query("SELECT * FROM workouts")
    fun getAllWorkoutsWithExercises(): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkoutsWithExercisesSync(): List<WorkoutWithExercises>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun getWorkoutWithExercises(workoutId: Long): Flow<WorkoutWithExercises?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutWithExercisesSync(workoutId: Long): WorkoutWithExercises?

    @Query("SELECT * FROM workouts")
    suspend fun getWorkouts(): List<WorkoutEntity>

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Transaction
    suspend fun upsertWorkoutWithExercises(
        workout: WorkoutEntity,
        exercises: List<ExerciseWithSets>,
    ) {
        val workoutId = insertWorkout(workout)
        // Note: CASCADE DELETE should handle removing old exercises if workout was replaced.
        // However, we ensure exercises have the correct workoutId.
        exercises.forEach { exWithSets ->
            val exerciseId = insertExercise(exWithSets.exercise.copy(workoutId = workoutId))
            exWithSets.sets.forEach { set ->
                insertSet(set.copy(exerciseId = exerciseId))
            }
        }
    }
}

@Dao
interface WorkoutSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkoutLogEntity): Long

    @Query("SELECT * FROM workout_logs WHERE masterExerciseId = :masterExerciseId ORDER BY timestamp ASC")
    fun getLogsForMasterExercise(masterExerciseId: Long): Flow<List<WorkoutLogEntity>>

    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessionsWithLogs(): Flow<List<SessionWithLogs>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getSessionWithLogs(sessionId: Long): Flow<SessionWithLogs?>

    @Query("SELECT * FROM workout_logs WHERE sessionId = :sessionId")
    fun getLogsForSession(sessionId: Long): Flow<List<WorkoutLogEntity>>
}

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity): Long

    @Query("SELECT * FROM achievements ORDER BY timestamp DESC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE type = :type")
    fun getAchievementsByType(type: String): Flow<List<AchievementEntity>>
}
