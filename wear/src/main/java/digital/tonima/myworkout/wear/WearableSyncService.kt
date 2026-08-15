package digital.tonima.myworkout.wear

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.myworkout.data.model.SyncData
import digital.tonima.myworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class WearableSyncService : WearableListenerService() {
    @Inject
    lateinit var repository: WorkoutRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/workout/definitions") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val workoutsJson = dataMap.getString("workouts_json")
                if (workoutsJson != null) {
                    processWorkoutsJson(workoutsJson)
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/workout/definitions") {
            val workoutsJson = String(messageEvent.data)
            processWorkoutsJson(workoutsJson)
        }
    }

    private fun processWorkoutsJson(json: String) {
        scope.launch {
            try {
                val syncData = Json.decodeFromString<SyncData>(json)
                // Save master exercises first to satisfy foreign keys
                syncData.masterExercises.forEach { master ->
                    repository.upsertMasterExercise(master)
                }
                syncData.workouts.forEach { workoutWithExercises ->
                    repository.addWorkout(
                        workoutWithExercises.workout,
                        workoutWithExercises.exercises,
                    )
                }
            } catch (e: Exception) {
                Log.e("WearableSyncService", "Error processing workouts JSON", e)
            }
        }
    }
}
