package dev.thomasharris.betterhtml

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan

// adapted from https://stackoverflow.com/questions/19444911/hr-tag-usage-in-android
class HrSpan(private val lineWidth: Int = 8) : ReplacementSpan() {
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return 0 // not sure why 0 works here
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {

        val style = paint.style
        val alpha = paint.alpha

        paint.style = Paint.Style.FILL
        paint.alpha = (255f * .7f).toInt()
        val middle = ((top + bottom) / 2).toFloat()
        val rect = RectF(0f, middle - (lineWidth / 2), canvas.width.toFloat(), middle + (lineWidth / 2))
        canvas.drawRoundRect(rect, lineWidth.toFloat(), lineWidth.toFloat(), paint)

        paint.style = style
        paint.alpha = alpha
    }
}
