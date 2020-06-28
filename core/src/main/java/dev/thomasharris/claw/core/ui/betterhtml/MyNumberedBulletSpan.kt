package dev.thomasharris.claw.core.ui.betterhtml

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan


class MyNumberedBulletSpan(
    private val indentation: Int,
    private val number: Int
) : LeadingMarginSpan {

    private val numberOffset = 8
    private val gapWidth = 32

    override fun getLeadingMargin(first: Boolean): Int {
        return (numberOffset * 2) + gapWidth
    }

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout?
    ) {

        if ((text as Spanned).getSpanStart(this) == start) {
            val style = paint.style
            val textAlign = paint.textAlign

            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.RIGHT

            // TODO handle different types of nested leading margins?
            //   eg. an ol in a blockquote
            val right =
                x + (indentation * getLeadingMargin(first)) + (getLeadingMargin(first) - numberOffset)

            canvas.drawText("$number.", right.toFloat(), baseline.toFloat(), paint)

            paint.style = style
            paint.textAlign = textAlign
        }
    }
}
