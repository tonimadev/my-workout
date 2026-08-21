package digital.tonima.myworkout.data.util

import android.media.AudioManager
import android.media.ToneGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneAlertManager
    @Inject
    constructor() : AlertManager {
        private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

        override fun triggerCompletionAlert() {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        }
    }
