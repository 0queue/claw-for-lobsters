package dev.thomasharris.claw.core.ui.betterhtml

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.widget.TextView

/**
 * Adapted from https://stackoverflow.com/questions/20856105/change-the-text-color-of-a-single-clickablespan-when-pressed-without-affecting-o
 */
class PressableLinkMovementMethod(var listener: ((String?) -> Unit)?) : LinkMovementMethod() {

    var pressedSpan: PressableSpan? = null

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressedSpan = getPressedSpan(widget, buffer, event)?.also { span ->
                    span.isPressed = true

                    // turns out selection does the color changing,
                    // and doesn't require the text to be selectable
                    Selection.setSelection(
                        buffer,
                        buffer.getSpanStart(span),
                        buffer.getSpanEnd(span)
                    )
                }

                if (pressedSpan == null)
                    listener?.invoke(null)

                pressedSpan != null
            }
            MotionEvent.ACTION_MOVE -> {

                if (pressedSpan != null && getPressedSpan(widget, buffer, event) != pressedSpan) {
                    pressedSpan?.let {
                        it.isPressed = false
                    }
                    Selection.removeSelection(buffer)

                    pressedSpan = null
                }

                pressedSpan != null
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedSpan?.let {
                    it.isPressed = false
                }
                pressedSpan = null
                Selection.removeSelection(buffer)
                true
            }
            MotionEvent.ACTION_UP -> {
                pressedSpan?.let {
                    it.isPressed = false
                    listener?.invoke(it.url)
                }
                pressedSpan = null
                Selection.removeSelection(buffer)
                true
            }
            else -> false
        }
    }

    private fun getPressedSpan(
        textView: TextView,
        spannable: Spannable,
        event: MotionEvent
    ): PressableSpan? {
        val x = event.x - textView.totalPaddingLeft + textView.scrollX
        val y = event.y - textView.totalPaddingTop + textView.scrollY

        val position = with(textView.layout) {
            getOffsetForHorizontal(getLineForVertical(y.toInt()), x)
        }

        val spans = spannable.getSpans(position, position, PressableSpan::class.java)
        return spans.getOrNull(0)?.let {
            spannable.tagAtPositionOrNull(position, it)
        }
    }
}

private fun <T : Any> Spannable.tagAtPositionOrNull(position: Int, tag: T): T? =
    if (position >= getSpanStart(tag) && position <= getSpanEnd(tag))
        tag
    else
        null
