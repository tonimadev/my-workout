package digital.tonima.myworkout.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import digital.tonima.myworkout.data.model.*

@Database(
    entities = [
        WorkoutEntity::class,
        MasterExerciseEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        WorkoutSessionEntity::class,
        WorkoutLogEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    abstract fun workoutSessionDao(): WorkoutSessionDao
}
