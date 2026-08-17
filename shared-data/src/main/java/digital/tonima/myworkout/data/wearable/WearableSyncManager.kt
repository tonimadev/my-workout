package digital.tonima.myworkout.data.wearable

import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.SyncData
import digital.tonima.myworkout.data.model.WorkoutLogEntity

interface WearableSyncManager {
    suspend fun syncWorkouts(syncData: SyncData)

    suspend fun syncLog(log: WorkoutLogEntity)

    suspend fun syncFinishSession(sessionId: Long)

    suspend fun syncSession(sessionWithLogs: SessionWithLogs)

    suspend fun sendMessage(
        path: String,
        data: ByteArray,
    )
}
