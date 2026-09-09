package digital.tonima.myworkout.wear.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.myworkout.data.util.AlertManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchAlertManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : AlertManager {
        private val vibrator: Vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

        override fun triggerCompletionAlert() {
            val timings = longArrayOf(0, 500, 200, 500)
            val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        }
    }
