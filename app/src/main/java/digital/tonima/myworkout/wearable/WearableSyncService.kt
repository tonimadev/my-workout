package digital.tonima.myworkout.wearable

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class WearableSyncService : WearableListenerService() {
    @Inject
    lateinit var repository: WorkoutRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            "/workout/log" -> {
                val logJson = String(messageEvent.data)
                scope.launch {
                    try {
                        val log = Json.decodeFromString<WorkoutLogEntity>(logJson)
                        repository.addLog(log)
                    } catch (e: Exception) {
                        Log.e("WearableSyncService", "Error adding log", e)
                    }
                }
            }
            "/workout/finish_session" -> {
                val sessionId = String(messageEvent.data).toLongOrNull() ?: return
                scope.launch {
                    try {
                        repository.getSessionById(sessionId).first()?.let {
                            repository.finishSession(it.session)
                        }
                    } catch (e: Exception) {
                        Log.e("WearableSyncService", "Error finishing session", e)
                    }
                }
            }
            "/workout/request_sync" -> {
                scope.launch {
                    repository.forceSync()
                }
            }
        }
    }
}
