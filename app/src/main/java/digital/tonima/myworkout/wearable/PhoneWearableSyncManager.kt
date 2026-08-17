package digital.tonima.myworkout.wearable

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.SyncData
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.wearable.WearableSyncManager
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneWearableSyncManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : WearableSyncManager {
        private val messageClient by lazy { Wearable.getMessageClient(context) }
        private val dataClient by lazy { Wearable.getDataClient(context) }

        override suspend fun syncWorkouts(syncData: SyncData) {
            Log.i("PhoneSyncManager", "Starting sync of ${syncData.workouts.size} workouts to wearable")
            try {
                val json = Json.encodeToString(syncData)
                val putDataMapReq =
                    PutDataMapRequest.create("/workout/definitions").apply {
                        dataMap.putString("workouts_json", json)
                        dataMap.putLong("timestamp", System.currentTimeMillis())
                    }
                val putDataReq = putDataMapReq.asPutDataRequest().setUrgent()
                dataClient.putDataItem(putDataReq).await()
                Log.i("PhoneSyncManager", "Successfully synced workouts to Data Client")
            } catch (e: Exception) {
                Log.e("PhoneSyncManager", "Error syncing workouts", e)
            }
        }

        override suspend fun syncLog(log: WorkoutLogEntity) {
            // Phone doesn't sync logs to watch in this flow
        }

        override suspend fun syncFinishSession(sessionId: Long) {
            // Phone doesn't sync finish to watch in this flow
        }

        override suspend fun syncSession(sessionWithLogs: SessionWithLogs) {
            // Phone doesn't sync full sessions back to watch
        }

        override suspend fun sendMessage(
            path: String,
            data: ByteArray,
        ) {
            Log.d("PhoneSyncManager", "Sending message to path: $path (${data.size} bytes)")
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                Log.d("PhoneSyncManager", "Found ${nodes.size} connected nodes")
                nodes.forEach { node ->
                    messageClient.sendMessage(node.id, path, data).await()
                    Log.d("PhoneSyncManager", "Message sent to node: ${node.displayName}")
                }
            } catch (e: Exception) {
                Log.e("PhoneSyncManager", "Error sending message: $path", e)
            }
        }
    }
