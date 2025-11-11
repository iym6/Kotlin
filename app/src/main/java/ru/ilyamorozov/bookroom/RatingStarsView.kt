package ru.ilyamorozov.bookroom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip
import kotlin.math.cos
import kotlin.math.sin

class RatingStarsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var rating: Float = 0f
    private val starPath = Path()
    private val clipPath = Path()

    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#FFD700".toColorInt()
        style = Paint.Style.FILL
    }

    private val emptyStarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#E0E0E0".toColorInt()
        style = Paint.Style.FILL
    }

    private val starSize = 65f
    private val starSpacing = 1f
    private val totalWidth = (starSize * 10) + (starSpacing * 9)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val startX = (width - totalWidth) / 2f
        var currentX = startX

        for (i in 0 until 10) {
            val starRating = (rating - i).coerceIn(0f, 1f)
            drawStar(canvas, currentX, starRating)
            currentX += starSize + starSpacing
        }
    }

    private fun drawStar(canvas: Canvas, x: Float, fillRatio: Float) {
        val centerY = height / 2f
        starPath.reset()
        createStarPath(starPath, x + starSize / 2f, centerY, starSize / 2f)
        canvas.drawPath(starPath, emptyStarPaint)

        if (fillRatio > 0f) {
            clipPath.reset()
            clipPath.addRect(x, 0f, x + starSize * fillRatio, height.toFloat(), Path.Direction.CW)
            canvas.withClip(clipPath) {
                drawPath(starPath, starPaint)
            }
        }
    }

    private fun createStarPath(path: Path, centerX: Float, centerY: Float, radius: Float) {
        val innerRadius = radius * 0.4f
        var angle = -Math.PI / 2
        val delta = Math.PI / 5

        path.moveTo(
            (centerX + radius * cos(angle)).toFloat(),
            (centerY + radius * sin(angle)).toFloat()
        )

        for (i in 1 until 10) {
            angle += delta
            val r = if (i % 2 == 0) radius else innerRadius
            path.lineTo(
                (centerX + r * cos(angle)).toFloat(),
                (centerY + r * sin(angle)).toFloat()
            )
        }
        path.close()
    }

    fun setRating(rating: Float) {
        this.rating = rating.coerceIn(0f, 10f)
        invalidate()
    }
}