package ru.ilyamorozov.torch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread
import kotlin.math.sin
import kotlin.random.Random

class TorchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    // Класс для искры
    data class Spark(val point: PointF, val color: Int)
    private val sparks = mutableListOf<Spark>()
    private val blockSizeDp = 50f
    private var blockSizePx = 0f
    private val torchWidthDp = 100f
    private val torchHeightDp = 400f

    init {
        holder.addCallback(this)
        // 20 искр
        for (i in 0 until 20) {
            val initialColor = when (Random.nextInt(4)) {
                0 -> ContextCompat.getColor(context, R.color.dark_orange)
                1 -> ContextCompat.getColor(context, R.color.yellow)
                2 -> ContextCompat.getColor(context, R.color.pale_yellow)
                else -> ContextCompat.getColor(context, R.color.orange)
            }
            sparks.add(Spark(PointF(Random.nextFloat() * width, Random.nextFloat() * height), initialColor))
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread {
            while (true) {
                val canvas = holder.lockCanvas()
                if (canvas != null) {
                    drawTorch(canvas)
                    holder.unlockCanvasAndPost(canvas)
                }
                Thread.sleep(16) // ~60 FPS
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        blockSizePx = dpToPx(blockSizeDp)
        sparks.forEach { it.point.x = Random.nextFloat() * w; it.point.y = Random.nextFloat() * h }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        blockSizePx = dpToPx(blockSizeDp)
        sparks.forEach { it.point.x = Random.nextFloat() * w; it.point.y = Random.nextFloat() * h }
    }

    private fun drawTorch(canvas: Canvas) {
        val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        val widthPx = width.toFloat()
        val heightPx = height.toFloat()
        val cols = (torchWidthDp / blockSizeDp).toInt().coerceAtLeast(2)
        val rows = (torchHeightDp / blockSizeDp).toInt().coerceAtLeast(8)

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // Просчет факела
        val torchWidthPx = dpToPx(torchWidthDp)
        val torchHeightPx = dpToPx(torchHeightDp)
        val torchLeft = (widthPx - torchWidthPx) / 2
        val torchTop = heightPx - torchHeightPx

        val colorPattern = arrayOf(
            intArrayOf(R.color.yellow, R.color.dark_orange),
            intArrayOf(R.color.pale_yellow, R.color.pale_white),
            intArrayOf(R.color.brown, R.color.pale_brown),
            intArrayOf(R.color.pale_brown, R.color.darker_brown),
            intArrayOf(R.color.dark_brown, R.color.brown),
            intArrayOf(R.color.brown, R.color.pale_brown),
            intArrayOf(R.color.dark_brown, R.color.darker_brown),
            intArrayOf(R.color.pale_brown, R.color.brown)
        )

        // Отрисовка факела
        for (y in 0 until rows) {
            val patternRow = minOf(y, colorPattern.size - 1)
            for (x in 0 until cols.coerceAtMost(2)) {
                val colorRes = colorPattern[patternRow][x % 2]
                paint.color = ContextCompat.getColor(context, colorRes)
                canvas.drawRect(
                    torchLeft + x * blockSizePx, torchTop + y * blockSizePx,
                    torchLeft + (x + 1) * blockSizePx, torchTop + (y + 1) * blockSizePx,
                    paint
                )
            }
        }

        // Отрисовка огоньков
        var sparkIndex = 0
        while (sparkIndex < sparks.size) {
            val spark = sparks[sparkIndex]
            if (spark.point.y <= torchTop) {
                paint.alpha = (sin(System.currentTimeMillis() / 100.0) * 127 + 128).toInt()
                paint.color = spark.color
                paint.alpha = (paint.alpha * 0.7).toInt()
                canvas.drawRect(
                    spark.point.x - blockSizePx / 2, spark.point.y - blockSizePx / 2,
                    spark.point.x + blockSizePx / 2, spark.point.y + blockSizePx / 2,
                    paint
                )
            }
            spark.point.y -= 0.5f * blockSizePx / 50f
            if (spark.point.y < 0 || spark.point.y > torchTop) {
                val newColor = when (Random.nextInt(4)) {
                    0 -> ContextCompat.getColor(context, R.color.dark_orange)
                    1 -> ContextCompat.getColor(context, R.color.yellow)
                    2 -> ContextCompat.getColor(context, R.color.pale_yellow)
                    else -> ContextCompat.getColor(context, R.color.orange)
                }
                spark.point.x = Random.nextFloat() * widthPx
                spark.point.y = torchTop + Random.nextFloat() * (heightPx - torchTop)
                sparks[sparkIndex] = Spark(spark.point, newColor)
            }
            sparkIndex++
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun minOf(a: Int, b: Int): Int {
        return if (a < b) a else b
    }
}