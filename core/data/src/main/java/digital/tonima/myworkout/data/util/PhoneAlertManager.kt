package digital.tonima.myworkout.data.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneAlertManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : AlertManager {
        private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

        override fun triggerCompletionAlert() {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
            vibrate()
        }

        // A tone alone is easy to miss on silent/vibrate-only phones, which is how most people
        // carry their phone mid-workout, so back it with a haptic buzz like the Wear OS app does.
        private fun vibrate() {
            val vibrator =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
            val timings = longArrayOf(0, 350, 150, 350)
            val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        }
    }
