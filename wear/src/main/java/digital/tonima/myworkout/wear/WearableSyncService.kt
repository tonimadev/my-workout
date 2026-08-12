package digital.tonima.myworkout.wear

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.myworkout.data.model.WorkoutWithExercises
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

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/workout/definitions") {
            val workoutsJson = String(messageEvent.data)
            scope.launch {
                try {
                    val workouts = Json.decodeFromString<List<WorkoutWithExercises>>(workoutsJson)
                    // Update local DB
                    workouts.forEach { workoutWithExercises ->
                        repository.addWorkout(
                            workoutWithExercises.workout,
                            workoutWithExercises.exercises
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
