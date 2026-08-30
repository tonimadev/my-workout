package digital.tonima.myworkout.wear

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.myworkout.R
import digital.tonima.myworkout.data.util.AlertManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class WorkoutService : Service() {
    @Inject
    lateinit var alertManager: AlertManager

    companion object {
        const val CHANNEL_ID = "workout_channel"
        const val NOTIFICATION_ID = 101
        const val ACTION_STOP = "digital.tonima.myworkout.wear.ACTION_STOP"
        const val ACTION_UPDATE_TIMER = "digital.tonima.myworkout.wear.ACTION_UPDATE_TIMER"

        private val _restTimeRemaining = MutableStateFlow(0L)
        val restTimeRemaining = _restTimeRemaining.asStateFlow()

        private val _totalRestTime = MutableStateFlow(0L)
        val totalRestTime = _totalRestTime.asStateFlow()

        private val _isResting = MutableStateFlow(false)
        val isResting = _isResting.asStateFlow()
    }

    private var workoutName: String = "Workout"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var restJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                restJob?.cancel()
                _isResting.value = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE_TIMER -> {
                handleActionUpdate(intent)
                return START_STICKY
            }
        }

        workoutName = intent?.getStringExtra("workout_name") ?: workoutName
        showNotification(0L)

        return START_STICKY
    }

    private fun handleActionUpdate(intent: Intent) {
        val restSeconds = intent.getIntExtra("rest_seconds", 0)
        if (restSeconds > 0) {
            val endTime = SystemClock.elapsedRealtime() + (restSeconds * 1000L)
            startRestTimer(restSeconds, endTime)
        } else {
            restJob?.cancel()
            _isResting.value = false
            _restTimeRemaining.value = 0
            showNotification(0L)
        }
    }

    private fun startRestTimer(
        seconds: Int,
        endTime: Long,
    ) {
        restJob?.cancel()
        _totalRestTime.value = seconds.toLong()
        _restTimeRemaining.value = seconds.toLong()
        _isResting.value = true

        showNotification(endTime)

        restJob =
            serviceScope.launch {
                var remaining = seconds.toLong()
                while (remaining > 0) {
                    delay(1000.milliseconds)
                    remaining--
                    _restTimeRemaining.value = remaining
                }
                _isResting.value = false
                alertManager.triggerCompletionAlert()
                showNotification(0L)
            }
    }

    private fun showNotification(restEndTime: Long) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        val statusBuilder = Status.Builder()
        val isRestingNow = restEndTime > SystemClock.elapsedRealtime()

        if (isRestingNow) {
            statusBuilder.addPart("rest", Status.TimerPart(restEndTime))
        } else {
            statusBuilder.addPart("workout", Status.TextPart("Workout: $workoutName"))
        }

        val notificationBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(
                    if (isRestingNow) {
                        "Resting..."
                    } else {
                        "Workout: $workoutName"
                    },
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setContentIntent(pendingIntent)

        val ongoingActivity =
            OngoingActivity.Builder(this, NOTIFICATION_ID, notificationBuilder)
                .setStaticIcon(R.drawable.ic_notification)
                .setTouchIntent(pendingIntent)
                .setStatus(statusBuilder.build())
                .build()

        ongoingActivity.apply(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notificationBuilder.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    override fun onDestroy() {
        restJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val name = "Workout Session"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel =
            NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = "Active workout tracking"
            }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
