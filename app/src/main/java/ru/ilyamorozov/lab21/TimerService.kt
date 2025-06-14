package ru.ilyamorozov.lab21

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class TimerService : Service() {
    private var timeLeft = 0L
    private var isRunning = false
    private lateinit var handler: Handler
    private lateinit var updateRunnable: Runnable
    private val binder = LocalBinder()
    private var notificationId = 1
    private val channelId = "timer_channel"

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        handler = Handler(Looper.getMainLooper())
        createNotificationChannel()
        updateRunnable = object : Runnable {
            override fun run() {
                if (timeLeft > 0 && isRunning) {
                    timeLeft -= 1000
                    updateNotification()
                    handler.postDelayed(this, 1000)
                } else {
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val minutes = intent?.getIntExtra("minutes", 0) ?: 0
        val seconds = intent?.getIntExtra("seconds", 0) ?: 0
        startTimer(minutes, seconds)
        return START_NOT_STICKY
    }

    fun startTimer(minutes: Int, seconds: Int) {
        if (isRunning) return

        timeLeft = (minutes * 60L + seconds) * 1000
        isRunning = true
        startForeground(notificationId, createNotification())
        handler.post(updateRunnable)
    }

    fun stopTimer() {
        if (!isRunning) return

        isRunning = false
        handler.removeCallbacks(updateRunnable)
        stopForeground(true)
    }

    fun isTimerRunning(): Boolean = isRunning

    fun getCurrentTime(): Long = timeLeft

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Timer Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(formatTime(timeLeft))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}