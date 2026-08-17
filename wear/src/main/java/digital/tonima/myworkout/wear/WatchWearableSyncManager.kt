package digital.tonima.myworkout.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.SyncData
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.wearable.WearableSyncManager
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
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
            Log.i("WatchSync", "Syncing log to phone for masterExerciseId: ${log.masterExerciseId}")
            try {
                val json = Json.encodeToString(log)
                sendMessage("/workout/log", json.toByteArray())
            } catch (e: Exception) {
                Log.e("WatchSync", "Error syncing log", e)
            }
        }

        override suspend fun syncFinishSession(sessionId: Long) {
            Log.i("WatchSync", "Syncing finish session status for ID: $sessionId")
            try {
                sendMessage("/workout/finish_session", sessionId.toString().toByteArray())
            } catch (e: Exception) {
                Log.e("WatchSync", "Error syncing finish session", e)
            }
        }

        override suspend fun syncSession(sessionWithLogs: SessionWithLogs) {
            Log.i("WatchSync", "Syncing full session to phone. Logs: ${sessionWithLogs.logs.size}")
            try {
                val json = Json.encodeToString(sessionWithLogs)
                sendMessage("/workout/session", json.toByteArray())
                Log.i("WatchSync", "Full session message sent")
            } catch (e: Exception) {
                Log.e("WatchSync", "Error syncing session", e)
            }
        }

        override suspend fun sendMessage(
            path: String,
            data: ByteArray,
        ) {
            Log.d("WatchSync", "Sending message to phone path: $path (${data.size} bytes)")
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                Log.d("WatchSync", "Found ${nodes.size} connected phone nodes")
                nodes.forEach { node ->
                    messageClient.sendMessage(node.id, path, data).await()
                    Log.d("WatchSync", "Message sent to node: ${node.displayName}")
                }
            } catch (e: Exception) {
                Log.e("WatchSync", "Error sending message", e)
            }
        }
    }
