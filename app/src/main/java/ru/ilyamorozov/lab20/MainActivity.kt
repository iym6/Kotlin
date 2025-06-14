package ru.ilyamorozov.lab20

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import ru.ilyamorozov.lab20.R.string.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val PERMISSION_REQUEST_CODE = 100
        const val EXTRA_COLOR = "extra_color"
        const val KEY_TEXT_REPLY = "key_text_reply"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        handleIntent(intent)

        findViewById<Button>(R.id.button).setOnClickListener {
            checkNotificationPermission()
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra(EXTRA_COLOR)?.let { colorHex ->
            runOnUiThread {
                try {
                    window.decorView.setBackgroundColor(Color.parseColor(colorHex))
                    findViewById<TextView>(R.id.textView).text =
                        getString(selected_color_text, colorHex)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(
                        this,
                        getString(invalid_color_toast),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(notification_channel_id),
                getString(notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(notification_channel_description)
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                showNotification()
            }
        } else {
            showNotification()
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification() {
        val notificationManager = NotificationManagerCompat.from(this)

        // Intent для открытия приложения
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Кнопка "Сбросить цвет"
        val resetIntent = Intent(this, MyReceiver::class.java).apply {
            action = getString(action_reset_color)
        }
        val resetPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            resetIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Кнопка "Задать цвет" с полем ввода
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(getString(color_input_hint))
            build()
        }
        val colorIntent = Intent(this, MyReceiver::class.java).apply {
            action = getString(action_set_color)
        }
        val colorPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                this,
                1,
                colorIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                this,
                1,
                colorIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val action = NotificationCompat.Action.Builder(
            R.drawable.ic_color,
            getString(set_color_action),
            colorPendingIntent
        ).addRemoteInput(remoteInput).build()

        // Построение уведомления
        val notification = NotificationCompat.Builder(this, getString(notification_channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(notification_title))
            .setContentText(getString(notification_content))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_reset, getString(reset_color_action), resetPendingIntent)
            .addAction(action)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            showNotification()
        }
    }
}