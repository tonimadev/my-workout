package digital.tonima.myworkout.data.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.myworkout.data.R
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

/**
 * Keeps the phone's rest-interval countdown alive and alerts the user when it ends, even if the
 * app is backgrounded or the screen is off. Mirrors the reliability of the Wear OS companion's
 * own foreground service: it only runs in the foreground for the duration of a rest interval,
 * and backs the countdown with an exact alarm so the completion alert still fires if the process
 * is killed mid-rest.
 */
@AndroidEntryPoint
class WorkoutTimerService : Service() {
    @Inject
    lateinit var alertManager: AlertManager

    companion object {
        private const val ONGOING_CHANNEL_ID = "workout_rest_ongoing"
        private const val COMPLETE_CHANNEL_ID = "workout_rest_complete"
        private const val NOTIFICATION_ID = 201

        const val ACTION_UPDATE_TIMER = "digital.tonima.myworkout.data.ACTION_UPDATE_TIMER"
        const val ACTION_STOP = "digital.tonima.myworkout.data.ACTION_STOP"
        const val ACTION_TIMER_EXPIRED = "digital.tonima.myworkout.data.ACTION_TIMER_EXPIRED"
        const val EXTRA_REST_SECONDS = "rest_seconds"

        private val _restTimeRemaining = MutableStateFlow(0)
        val restTimeRemaining = _restTimeRemaining.asStateFlow()

        private val _totalRestTime = MutableStateFlow(0)
        val totalRestTime = _totalRestTime.asStateFlow()
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_UPDATE_TIMER -> {
                val seconds = intent.getIntExtra(EXTRA_REST_SECONDS, 0)
                if (seconds > 0) startRestTimer(seconds) else stopSelfCleanly()
            }
            ACTION_TIMER_EXPIRED -> onTimerExpired()
            ACTION_STOP -> stopSelfCleanly()
        }
        return START_NOT_STICKY
    }

    private fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        cancelAlarm()

        val endTime = SystemClock.elapsedRealtime() + seconds * 1000L
        _totalRestTime.value = seconds
        _restTimeRemaining.value = seconds

        showOngoingNotification()
        scheduleAlarm(endTime)

        timerJob =
            serviceScope.launch {
                while (SystemClock.elapsedRealtime() < endTime) {
                    val remaining = (endTime - SystemClock.elapsedRealtime() + 999) / 1000L
                    _restTimeRemaining.value = remaining.toInt().coerceAtLeast(0)
                    delay(500.milliseconds)
                }
                onTimerExpired()
            }
    }

    private fun onTimerExpired() {
        timerJob?.cancel()
        cancelAlarm()
        _restTimeRemaining.value = 0
        _totalRestTime.value = 0
        alertManager.triggerCompletionAlert()
        showCompletionNotification()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopSelfCleanly() {
        timerJob?.cancel()
        cancelAlarm()
        _restTimeRemaining.value = 0
        _totalRestTime.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun scheduleAlarm(endTimeMillis: Long) {
        val alarmManagerService = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = expiredPendingIntent(create = true) ?: return
        try {
            alarmManagerService.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                endTimeMillis,
                pendingIntent,
            )
        } catch (e: SecurityException) {
            alarmManagerService.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                endTimeMillis,
                pendingIntent,
            )
        }
    }

    private fun cancelAlarm() {
        val alarmManagerService = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        expiredPendingIntent(create = false)?.let { alarmManagerService.cancel(it) }
    }

    private fun expiredPendingIntent(create: Boolean): PendingIntent? {
        val intent = Intent(this, WorkoutTimerService::class.java).apply { action = ACTION_TIMER_EXPIRED }
        val flags =
            (if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE) or
                PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getService(this, 0, intent, flags)
    }

    private fun showOngoingNotification() {
        val notification =
            NotificationCompat.Builder(this, ONGOING_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_resting_title))
                .setContentText(getString(R.string.notification_resting_text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setContentIntent(openAppPendingIntent())
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showCompletionNotification() {
        val notification =
            NotificationCompat.Builder(this, COMPLETE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_rest_complete_title))
                .setContentText(getString(R.string.notification_rest_complete_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                // The alert itself (tone + vibration) is delivered by AlertManager; keep this
                // notification silent so the two don't stack.
                .setVibrate(longArrayOf(0))
                .setContentIntent(openAppPendingIntent())
                .build()

        val notificationManager = NotificationManagerCompat.from(this)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun openAppPendingIntent(): PendingIntent? {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        return PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                ONGOING_CHANNEL_ID,
                getString(R.string.notification_channel_ongoing_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = getString(R.string.notification_channel_ongoing_desc) },
        )
        notificationManager.createNotificationChannel(
            NotificationChannel(
                COMPLETE_CHANNEL_ID,
                getString(R.string.notification_channel_complete_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = getString(R.string.notification_channel_complete_desc) },
        )
    }

    override fun onDestroy() {
        cancelAlarm()
        timerJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
