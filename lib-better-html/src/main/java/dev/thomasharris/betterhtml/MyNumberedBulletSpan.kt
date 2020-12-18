package dev.thomasharris.betterhtml

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan

class MyNumberedBulletSpan(
    private val indentation: Int,
    private val number: Int,
    max: Int
) : LeadingMarginSpan {

    private val isLong = max >= 10

    private val numberOffset = if (isLong) 4 else 8

    // makes short lists and long lists look nice
    override fun getLeadingMargin(first: Boolean): Int {
        return if (isLong) LEADING_MARGIN * 2 else LEADING_MARGIN
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
            val textAlign = paint.textAlign

            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.RIGHT

            // TODO nested ol
            val right =
                x + (indentation * LEADING_MARGIN) + (getLeadingMargin(first) - numberOffset)

            canvas.drawText("$number.", right.toFloat(), baseline.toFloat(), paint)

            paint.style = style
            paint.textAlign = textAlign
        }
    }
}
