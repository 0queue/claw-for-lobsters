package dev.thomasharris.betterhtml

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan

class MyBulletSpan(
    private val indentation: Int
) : LeadingMarginSpan {

    private val bulletRadius = 8

    override fun getLeadingMargin(first: Boolean): Int {
        return LEADING_MARGIN
    }

    override fun drawLeadingMargin(
        canvas: Canvas,
        paint: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        if ((text as Spanned).getSpanStart(this) == start) {
            val style = paint.style
            paint.style = Paint.Style.FILL

            val yPosition = (top + bottom) / 2f

            // no longer respects text direction, sorry
            val xPosition =
                x + (getLeadingMargin(first) * indentation) + (getLeadingMargin(first) / 2f)
            canvas.drawCircle(xPosition, yPosition, bulletRadius.toFloat(), paint)

            paint.style = style
        }
    }
}
