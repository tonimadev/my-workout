package digital.tonima.myworkout.wearable

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.myworkout.data.model.SessionWithLogs
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
        Log.i("WearableSyncService", "Message received from wear: ${messageEvent.path}")
        when (messageEvent.path) {
            "/workout/log" -> {
                val logJson = String(messageEvent.data)
                scope.launch {
                    try {
                        val log = Json.decodeFromString<WorkoutLogEntity>(logJson)
                        Log.d("WearableSyncService", "Processing log for exerciseId: ${log.masterExerciseId}")
                        repository.addLog(log)
                    } catch (e: Exception) {
                        Log.e("WearableSyncService", "Error adding log", e)
                    }
                }
            }
            "/workout/session" -> {
                val sessionJson = String(messageEvent.data)
                scope.launch {
                    try {
                        val sessionWithLogs = Json.decodeFromString<SessionWithLogs>(sessionJson)
                        Log.i(
                            "WearableSyncService",
                            "Received full session from wear. Logs count: ${sessionWithLogs.logs.size}",
                        )
                        repository.saveRemoteSession(sessionWithLogs)
                        Log.i("WearableSyncService", "Full session saved successfully")
                    } catch (e: Exception) {
                        Log.e("WearableSyncService", "Error saving remote session", e)
                    }
                }
            }
            "/workout/finish_session" -> {
                val sessionId = String(messageEvent.data).toLongOrNull() ?: return
                Log.i("WearableSyncService", "Finish session request received for ID: $sessionId")
                scope.launch {
                    try {
                        repository.getSessionById(sessionId).first()?.let {
                            repository.finishSession(it.session)
                            Log.d("WearableSyncService", "Session $sessionId finished on phone")
                        }
                    } catch (e: Exception) {
                        Log.e("WearableSyncService", "Error finishing session", e)
                    }
                }
            }
            "/workout/request_sync" -> {
                Log.i("WearableSyncService", "Wear requested manual sync")
                scope.launch {
                    repository.forceSync()
                }
            }
        }
    }
}
