package com.verve.emovision.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

data class FaceBox(val box: Rect, val label: String)

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paintBox =
        Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

    private val paintText =
        Paint().apply {
            color = Color.BLACK
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

    private var faceBoxes: List<FaceBox> = emptyList()

    fun updateFaces(faces: List<FaceBox>) {
        faceBoxes = faces
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (face in faceBoxes) {
            val box = face.box

            // Dapatkan ukuran sisi terbesar untuk membuat kotak tetap persegi
            val boxSize = maxOf(box.width(), box.height())

            // Hitung titik tengah bounding box wajah
            val centerX = box.centerX()
            val centerY = box.centerY()

            // Hitung koordinat baru agar kotak tetap di tengah wajah
            val left = (centerX - boxSize / 2).coerceAtLeast(0)
            val top = (centerY - boxSize / 2).coerceAtLeast(0)
            val right = (centerX + boxSize / 2).coerceAtMost(width)
            val bottom = (centerY + boxSize / 2).coerceAtMost(height)

            // Buat kotak baru dengan ukuran persegi
            val squareBox = Rect(left, top, right, bottom)

            // Gambar kotak
            canvas.drawRect(squareBox, paintBox)

            // Gambar label di atas kotak
            val textPadding = 10f
            val textWidth = paintText.measureText(face.label) + 2 * textPadding
            val textHeight = paintText.textSize + 2 * textPadding
            val x = left.toFloat()
            val y = top.toFloat() - textHeight

            val labelBackground =
                Paint().apply {
                    color = Color.YELLOW
                    style = Paint.Style.FILL
                }

            // Pastikan label tidak keluar dari layar
            val labelRect = RectF(x, y, x + textWidth, y + textHeight)
            canvas.drawRect(labelRect, labelBackground)
            canvas.drawText(face.label, x + textPadding, y + textHeight - textPadding, paintText)
        }
    }
}
