package digital.tonima.myworkout.data.util

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.myworkout.data.service.WorkoutTimerService
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneRestTimerController
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : RestTimerController {
        override val restTimeRemaining: StateFlow<Int> = WorkoutTimerService.restTimeRemaining
        override val totalRestTime: StateFlow<Int> = WorkoutTimerService.totalRestTime

        override fun startRest(seconds: Int) {
            if (seconds <= 0) {
                stop()
                return
            }
            val intent =
                Intent(context, WorkoutTimerService::class.java).apply {
                    action = WorkoutTimerService.ACTION_UPDATE_TIMER
                    putExtra(WorkoutTimerService.EXTRA_REST_SECONDS, seconds)
                }
            context.startForegroundService(intent)
        }

        override fun stop() {
            val intent =
                Intent(context, WorkoutTimerService::class.java).apply {
                    action = WorkoutTimerService.ACTION_STOP
                }
            context.startService(intent)
        }
    }
