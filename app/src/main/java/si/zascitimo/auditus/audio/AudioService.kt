package si.zascitimo.auditus.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import si.zascitimo.auditus.App
import si.zascitimo.auditus.R

class AudioService : LifecycleService() {

    private val audioRouter by lazy { (application as App).audioRouter }
    private val audioStatus by lazy { (application as App).audioStatus }

    override fun onCreate() {
        super.onCreate()

        val notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, "Audio", importance)
            channel.description = "Audio stream"
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.setShowBadge(false)
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_INIT -> init()
            ACTION_START -> start()
            ACTION_STOP -> {
                stopForeground(true)
                audioRouter.stop()
            }
            ACTION_DESTROY -> {
                audioRouter.destroy()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun init() {
        if (audioRouter.init()) {
            audioStatus.observe(this, Observer {
                if (it.isStreamActive) {
                    updateNotificationState()
                }
            })
        }
    }

    private fun start() {
        audioRouter.init()
        if (audioRouter.start()) {
            startForeground(
                1,
                buildNotification()
            )
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getService(
            this,
            1,
            destroyIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.stream_active))
            .setLargeIcon(AppCompatResources.getDrawable(this,
                R.drawable.ic_play
            )?.toBitmap() )
            .setOngoing(true)
            .addAction(0, getString(R.string.stop), pi)
            .build()
    }

    fun updateNotificationState() {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1, buildNotification())
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        audioRouter.destroy()

        stopForeground(true)

        super.onDestroy()
    }

    companion object {
        private const val ACTION_INIT = "init"
        private const val ACTION_START = "start"
        private const val ACTION_STOP = "stop"
        private const val ACTION_DESTROY = "destroy"

        private const val CHANNEL_ID = "audio"

        fun initIntent(context: Context): Intent =
            Intent(ACTION_INIT, null, context, AudioService::class.java)

        fun startIntent(context: Context): Intent =
            Intent(ACTION_START, null, context, AudioService::class.java)

        fun stopIntent(context: Context): Intent =
            Intent(ACTION_STOP, null, context, AudioService::class.java)

        fun destroyIntent(context: Context): Intent =
            Intent(ACTION_DESTROY, null, context, AudioService::class.java)
    }
}