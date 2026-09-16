package digital.tonima.myworkout.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import digital.tonima.myworkout.data.model.AchievementEntity
import digital.tonima.myworkout.data.model.ExerciseEntity
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.model.WorkoutSessionEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Upsert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Upsert
    suspend fun insertMasterExercise(exercise: MasterExerciseEntity): Long

    @Query("SELECT * FROM master_exercises")
    fun getAllMasterExercises(): Flow<List<MasterExerciseEntity>>

    @Upsert
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Upsert
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
        // @Upsert updates existing rows in place (no delete+reinsert), so cascading
        // children (exercises/sets and, transitively, this session's workout_logs)
        // are never wiped out just because the template was edited mid-workout.
        // Its return value is only meaningful for a brand-new row (id == 0); for an
        // existing row we already know the real id, so prefer that.
        val insertedWorkoutId = insertWorkout(workout)
        val workoutId = if (workout.id != 0L) workout.id else insertedWorkoutId
        exercises.forEach { exWithSets ->
            val insertedExerciseId = insertExercise(exWithSets.exercise.copy(workoutId = workoutId))
            val exerciseId = if (exWithSets.exercise.id != 0L) exWithSets.exercise.id else insertedExerciseId
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

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getActiveSession(): Flow<SessionWithLogs?>

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
