package digital.tonima.myworkout.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.myworkout.data.model.SyncData
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.wearable.WearableSyncManager
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchWearableSyncManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : WearableSyncManager {
        private val messageClient by lazy { Wearable.getMessageClient(context) }

        override suspend fun syncWorkouts(syncData: SyncData) {
            // Watch doesn't sync definitions to Phone
        }

        override suspend fun syncLog(log: WorkoutLogEntity) {
            try {
                val json = Json.encodeToString(log)
                sendMessage("/workout/log", json.toByteArray())
            } catch (e: Exception) {
                Log.e("WatchSync", "Error syncing log", e)
            }
        }

        override suspend fun syncFinishSession(sessionId: Long) {
            try {
                sendMessage("/workout/finish_session", sessionId.toString().toByteArray())
            } catch (e: Exception) {
                Log.e("WatchSync", "Error syncing finish session", e)
            }
        }

        override suspend fun sendMessage(
            path: String,
            data: ByteArray,
        ) {
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                nodes.forEach { node ->
                    messageClient.sendMessage(node.id, path, data).await()
                }
            } catch (e: Exception) {
                Log.e("WatchSync", "Error sending message", e)
            }
        }
    }
