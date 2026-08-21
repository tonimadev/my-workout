package digital.tonima.myworkout.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import digital.tonima.myworkout.data.model.AchievementEntity
import digital.tonima.myworkout.data.model.ExerciseEntity
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.model.WorkoutSessionEntity

@Database(
    entities = [
        WorkoutEntity::class,
        MasterExerciseEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        WorkoutSessionEntity::class,
        WorkoutLogEntity::class,
        AchievementEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    abstract fun workoutSessionDao(): WorkoutSessionDao

    abstract fun achievementDao(): AchievementDao
}
