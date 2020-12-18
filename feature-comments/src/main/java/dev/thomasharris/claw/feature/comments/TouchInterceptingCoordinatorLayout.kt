package dev.thomasharris.claw.feature.comments

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout

@SuppressLint("ClickableViewAccessibility")
class TouchInterceptingCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    var listener: Listener? = null

    init {
        setOnTouchListener { v: View, e: MotionEvent ->
            return@setOnTouchListener listener?.onTouch(v, e) ?: false
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return (listener?.onInterceptTouchEvent(this, ev) ?: false) ||
            super.onInterceptTouchEvent(ev)
    }

    interface Listener {
        fun onInterceptTouchEvent(view: View, ev: MotionEvent): Boolean
        fun onTouch(view: View, event: MotionEvent): Boolean
    }
}
