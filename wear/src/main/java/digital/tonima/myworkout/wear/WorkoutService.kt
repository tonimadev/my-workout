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
import digital.tonima.myworkout.R

class WorkoutService : Service() {
    companion object {
        const val CHANNEL_ID = "workout_channel"
        const val NOTIFICATION_ID = 101
        const val ACTION_STOP = "digital.tonima.myworkout.wear.ACTION_STOP"
        const val ACTION_UPDATE_TIMER = "digital.tonima.myworkout.wear.ACTION_UPDATE_TIMER"
    }

    private var workoutName: String = "Workout"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_UPDATE_TIMER) {
            handleActionUpdate(intent)
            return START_STICKY
        }

        workoutName = intent?.getStringExtra("workout_name") ?: "Workout"
        val workoutId = intent?.getLongExtra("workout_id", -1L) ?: -1L

        val notificationIntent =
            Intent(this, MainActivity::class.java).apply {
                putExtra("workout_id", workoutId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        val notificationBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Workout: $workoutName")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setContentIntent(pendingIntent)

        val ongoingActivity =
            OngoingActivity.Builder(this, NOTIFICATION_ID, notificationBuilder)
                .setStaticIcon(R.drawable.ic_notification)
                .setTouchIntent(pendingIntent)
                .setStatus(Status.Builder().addPart("workout", Status.TextPart("Workout: $workoutName")).build())
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

        return START_STICKY
    }

    private fun handleActionUpdate(intent: Intent?) {
        val endTime = intent?.getLongExtra("rest_end_time", 0L) ?: 0L
        updateOngoingActivity(endTime)
    }

    private fun updateOngoingActivity(restEndTime: Long) {
        val notificationIntent =
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        val statusBuilder = Status.Builder()
        if (restEndTime > SystemClock.elapsedRealtime()) {
            statusBuilder.addPart("rest", Status.TimerPart(restEndTime))
        } else {
            statusBuilder.addPart("workout", Status.TextPart("Workout: $workoutName"))
        }

        val notificationBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(
                    if (restEndTime > SystemClock.elapsedRealtime()) {
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
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
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
