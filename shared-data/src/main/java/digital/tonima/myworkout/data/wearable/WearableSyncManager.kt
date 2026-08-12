package digital.tonima.myworkout.data.wearable

import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises

interface WearableSyncManager {
    suspend fun syncWorkouts(workouts: List<WorkoutWithExercises>)
    suspend fun syncLog(log: WorkoutLogEntity)
    suspend fun sendMessage(path: String, data: ByteArray)
}
