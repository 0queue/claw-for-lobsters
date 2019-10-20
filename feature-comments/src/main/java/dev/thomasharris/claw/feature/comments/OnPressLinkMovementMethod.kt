package dev.thomasharris.claw.feature.comments

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView

/**
 * Adapted from https://stackoverflow.com/questions/20856105/change-the-text-color-of-a-single-clickablespan-when-pressed-without-affecting-o
 */
class OnPressLinkMovementMethod(var listener: ((String?) -> Unit)?) : LinkMovementMethod() {

    var pressedSpan: ClickableSpan? = null

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressedSpan = getPressedSpan(widget, buffer, event)
                Log.i("TEH", "pressed span: $pressedSpan")
                pressedSpan?.let {
                    // TODO set pressed here
                    (pressedSpan as? PressableSpan)?.let { pressable ->
                        pressable.isPressed = true
                    }
                    // Do I event want to bother with Selection here??
                    Selection.setSelection(buffer, buffer.getSpanStart(it), buffer.getSpanEnd(it))
                }
                if (pressedSpan == null) {
                    listener?.invoke(null)
                }
                pressedSpan != null
            }
            MotionEvent.ACTION_MOVE -> {
                Log.i("TEH", "Moving...")
                val newSpan = getPressedSpan(widget, buffer, event)
                if (pressedSpan != null && newSpan != pressedSpan) {
                    (pressedSpan as? PressableSpan)?.let {
                        it.isPressed = false
                    }
                    Log.i("TEH", "unset!!")
                    pressedSpan = null
                    // blah selection again?
                    Selection.removeSelection(buffer)
                }
                pressedSpan != null
            }
            MotionEvent.ACTION_CANCEL -> {
                (pressedSpan as? PressableSpan)?.let {
                    it.isPressed = false
                }
                pressedSpan = null
                Selection.removeSelection(buffer)
                true
            }
            MotionEvent.ACTION_UP -> {
                Log.i("TEH", "ACTION_UP")
                if (pressedSpan != null) {
                    (pressedSpan as? PressableSpan)?.let {
                        it.isPressed = false
                        listener?.invoke(it.url)
                    }
                    pressedSpan = null
//                    super.onTouchEvent(widget, buffer, event)
                }
                Selection.removeSelection(buffer)
                true
            }
            else -> {
                Log.i("TEH", "Other event action: ${event.action}")
                false
            }
        }
    }

    private fun getPressedSpan(
        textView: TextView,
        spannable: Spannable,
        event: MotionEvent
    ): ClickableSpan? {
        val x = event.x - textView.totalPaddingLeft + textView.scrollX
        val y = event.y - textView.totalPaddingTop + textView.scrollY

        val position = with(textView.layout) {
            getOffsetForHorizontal(getLineForVertical(y.toInt()), x)
        }

        val spans = spannable.getSpans(position, position, ClickableSpan::class.java)
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
