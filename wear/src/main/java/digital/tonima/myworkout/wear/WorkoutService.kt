package digital.tonima.myworkout.wear

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import digital.tonima.myworkout.R

class WorkoutService : Service() {
    companion object {
        const val CHANNEL_ID = "workout_channel"
        const val NOTIFICATION_ID = 101
        const val ACTION_STOP = "digital.tonima.myworkout.wear.ACTION_STOP"
    }

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

        val workoutName = intent?.getStringExtra("workout_name") ?: "Workout"

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        val notificationBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_workout_wear_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Workout: $workoutName")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setContentIntent(pendingIntent)

        val ongoingActivity =
            OngoingActivity.Builder(this, NOTIFICATION_ID, notificationBuilder)
                .setStaticIcon(R.drawable.ic_workout_wear_launcher)
                .setTouchIntent(pendingIntent)
                .setStatus(Status.Builder().addPart("workout", Status.TextPart("Workout: $workoutName")).build())
                .build()

        ongoingActivity.apply(this)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        return START_STICKY
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
