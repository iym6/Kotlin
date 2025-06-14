package ru.ilyamorozov.lab21

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.ilyamorozov.lab21.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var timerService: TimerService? = null
    private var isBound = false
    private var isTimerRunningLocally = false
    private var currentTimeMillis = 0L

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.LocalBinder
            timerService = binder.getService()
            isBound = true

            if (timerService?.isTimerRunning() == true) {
                // Если сервис уже работает, синхронизируем состояние
                isTimerRunningLocally = true
                currentTimeMillis = timerService!!.getCurrentTime()
                updateUI(true)
                startLocalTimerUpdate()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startStopButton.setOnClickListener {
            if (isTimerRunningLocally) {
                // Остановка таймера
                timerService?.stopTimer()
                isTimerRunningLocally = false
                updateUI(false)
                binding.timerStatusText.text = getString(R.string.timer_ready)
            } else {
                // Запуск таймера
                val minutes = binding.minutesEditText.text.toString().toIntOrNull() ?: 0
                val seconds = binding.secondsEditText.text.toString().toIntOrNull() ?: 0

                if (minutes == 0 && seconds == 0) {
                    Toast.makeText(this, getString(R.string.set_time), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                currentTimeMillis = (minutes * 60L + seconds) * 1000
                isTimerRunningLocally = true
                updateUI(true)
                binding.timerStatusText.text = formatTime(currentTimeMillis)

                // Запускаем локальное обновление UI сразу
                startLocalTimerUpdate()

                // Запускаем сервис
                val intent = Intent(this, TimerService::class.java).apply {
                    putExtra("minutes", minutes)
                    putExtra("seconds", seconds)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private fun startLocalTimerUpdate() {
        binding.timerStatusText.postDelayed(object : Runnable {
            override fun run() {
                if (isTimerRunningLocally) {
                    currentTimeMillis -= 1000

                    if (currentTimeMillis <= 0) {
                        currentTimeMillis = 0
                        isTimerRunningLocally = false
                        updateUI(false)
                        binding.timerStatusText.text = getString(R.string.timer_ready)
                    } else {
                        binding.timerStatusText.text = formatTime(currentTimeMillis)
                        binding.timerStatusText.postDelayed(this, 1000)
                    }
                }
            }
        }, 1000)
    }

    private fun updateUI(isRunning: Boolean) {
        binding.minutesEditText.isEnabled = !isRunning
        binding.secondsEditText.isEnabled = !isRunning
        binding.startStopButton.text = if (isRunning) getString(R.string.stop_button) else getString(R.string.start_button)
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, TimerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}