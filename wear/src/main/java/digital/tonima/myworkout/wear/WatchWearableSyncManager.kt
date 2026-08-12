package digital.tonima.myworkout.wear

import android.content.Context
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import digital.tonima.myworkout.data.wearable.WearableSyncManager
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchWearableSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) : WearableSyncManager {

    private val messageClient by lazy { Wearable.getMessageClient(context) }

    override suspend fun syncWorkouts(workouts: List<WorkoutWithExercises>) {
        // Watch doesn't sync definitions to Phone
    }

    override suspend fun syncLog(log: WorkoutLogEntity) {
        try {
            val json = Json.encodeToString(log)
            sendMessage("/workout/log", json.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun sendMessage(path: String, data: ByteArray) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, path, data).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
