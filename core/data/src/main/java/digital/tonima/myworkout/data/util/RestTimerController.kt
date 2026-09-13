package digital.tonima.myworkout.data.util

import kotlinx.coroutines.flow.StateFlow

/**
 * Drives the rest-interval countdown between sets. The Android implementation keeps the
 * countdown alive and the user informed even if the app is backgrounded or the screen turns
 * off, mirroring the reliability the Wear OS companion app already has via its own foreground
 * service.
 */
interface RestTimerController {
    val restTimeRemaining: StateFlow<Int>
    val totalRestTime: StateFlow<Int>

    fun startRest(seconds: Int)

    fun stop()
}
