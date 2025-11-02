package ru.ilyamorozov.cameracolor

import android.graphics.*
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import androidx.core.graphics.createBitmap

typealias ColorUpdateListener = (hex: String, bitmap: Bitmap) -> Unit

class CentralPixelAnalyzer(
    private val listener: ColorUpdateListener
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val width = image.width
        val height = image.height
        val rowStride = image.planes[0].rowStride

        val centerX = width / 2
        val centerY = height / 2

        if (centerX >= width || centerY >= height) {
            image.close()
            return
        }

        val offset = centerY * rowStride + centerX * 4
        buffer.position(offset)

        val r = buffer.get().toInt() and 0xFF
        val g = buffer.get().toInt() and 0xFF
        val b = buffer.get().toInt() and 0xFF

        val hexColor = String.format("#%02X%02X%02X", r, g, b)
        val colorInt = Color.rgb(r, g, b)

        val size = 48
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { color = colorInt }

        val radius = 12f
        canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), radius, radius, paint)

        listener(hexColor, bitmap)
        image.close()
    }
}