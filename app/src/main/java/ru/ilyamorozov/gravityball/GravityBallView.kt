package ru.ilyamorozov.gravityball

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

class GravityBallView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SensorEventListener {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private var ballX = 0f
    private var ballY = 0f
    private var velocityX = 0f
    private var velocityY = 0f
    private var ballRadius = 60f

    private var lastTime = System.currentTimeMillis()

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gravitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    init {
        // Центрируем шарик при старте
        post {
            ballX = width / 2f
            ballY = height / 2f
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        gravitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onDetachedFromWindow() {
        sensorManager.unregisterListener(this)
        super.onDetachedFromWindow()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_GRAVITY) return

        val currentTime = System.currentTimeMillis()
        val dt = (currentTime - lastTime) / 1000f // в секундах
        lastTime = currentTime

        if (dt > 0.1f) return // пропускаем слишком большие задержки

        // Ускорение от гравитации (в м/с²)
        val ax = event.values[0] // X: лево-право
        val ay = event.values[1] // Y: верх-низ

        // t ≈ 1, поэтому V = V0 + a
        velocityX += -ax * dt * 50 // масштабный коэффициент для видимости
        velocityY += ay * dt * 50

        // Обновляем позицию: S = S0 + V
        ballX += velocityX * dt * 100
        ballY += velocityY * dt * 100

        // Проверка границ с отскоком
        val left = ballRadius
        val right = width - ballRadius
        val top = ballRadius
        val bottom = height - ballRadius

        if (ballX <= left) {
            ballX = left
            velocityX = abs(velocityX) * 0.8f // затухание
        } else if (ballX >= right) {
            ballX = right
            velocityX = -abs(velocityX) * 0.8f
        }

        if (ballY <= top) {
            ballY = top
            velocityY = abs(velocityY) * 0.8f
        } else if (ballY >= bottom) {
            ballY = bottom
            velocityY = -abs(velocityY) * 0.8f
        }

        invalidate() // перерисовка
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(ballX, ballY, ballRadius, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (ballX == 0f || ballY == 0f) {
            ballX = w / 2f
            ballY = h / 2f
        }
    }
}