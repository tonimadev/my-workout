package digital.tonima.myworkout.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Immutable
@Serializable
@Entity(tableName = "master_exercises")
data class MasterExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
)

@Immutable
@Serializable
@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
)

@Immutable
@Serializable
@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MasterExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["masterExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val masterExerciseId: Long = 0, // Default for migration
    val name: String,
    val order: Int,
)

@Immutable
@Serializable
@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val targetWeight: Double,
    val targetReps: Int,
    val restInterval: Int = 60,
    val notes: String = "",
    val order: Int,
)

@Immutable
@Serializable
@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long?,
    val startTime: Long,
    val endTime: Long? = null,
    val totalVolume: Double = 0.0,
    val xpGained: Int = 0,
)

@Immutable
@Serializable
@Entity(
    tableName = "workout_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MasterExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["masterExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SetEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val masterExerciseId: Long = 0, // Default for migration
    val setId: Long,
    val actualWeight: Double,
    val actualReps: Int,
    val actualNotes: String = "",
    val timestamp: Long,
)

@Immutable
@Serializable
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val name: String,
    val description: String,
    val timestamp: Long,
    val level: Int = 1,
)

@Immutable
@Serializable
data class WorkoutWithExercises(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        entity = ExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "workoutId",
    )
    val exercises: List<ExerciseWithSets>,
)

@Immutable
@Serializable
data class ExerciseWithSets(
    @Embedded val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId",
    )
    val sets: List<SetEntity>,
)

@Immutable
@Serializable
data class SessionWithLogs(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "workoutId",
        entityColumn = "id",
    )
    val workout: WorkoutEntity? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId",
    )
    val logs: List<WorkoutLogEntity>,
)

@Serializable
data class SyncData(
    val workouts: List<WorkoutWithExercises>,
    val masterExercises: List<MasterExerciseEntity>,
)
