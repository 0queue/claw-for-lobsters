package dev.thomasharris.feature.frontpage

import android.graphics.*
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt

/**
 * Copied from an old project,
 * I remember this took a lot of
 * trial and error
 */
class TagSpan(
    private val paddingPx: Float = 8f,
    private val textScale: Float = .8f,
    private val cornerRadiusPx: Float = 8f,
    @field:ColorInt private val borderColor: Int = Color.rgb(0xff, 0xfc, 0xd7),
    @field:ColorInt private val backgroundColor: Int = Color.rgb(0xff, 0xfc, 0xd7)
) : ReplacementSpan() {

    // default text paint
    private var textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 12f
        typeface = Typeface.DEFAULT
    }

    private val backgroundPaint = Paint().apply {
        color = backgroundColor
    }

    private val borderPaint = Paint().apply {
        color = borderColor
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        textPaint = Paint(paint).apply {
            textSize *= textScale
            typeface = Typeface.DEFAULT
            color = Color.BLACK
        }

        return paddingPx.toInt() + textPaint.measureText(text, start, end).toInt() + paddingPx.toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val width = textPaint.measureText(text, start, end)
        val height = textPaint.fontMetrics.bottom - textPaint.fontMetrics.top
        val vshift = borderPaint.strokeWidth / 2
        val rect = RectF(
            x,
            bottom.toFloat() - height - vshift,
            x + width + paddingPx * 2f,
            bottom.toFloat() - vshift
        )

        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, backgroundPaint)
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, borderPaint)
        canvas.drawText(text, start, end, x + paddingPx, y.toFloat(), textPaint)
    }

}