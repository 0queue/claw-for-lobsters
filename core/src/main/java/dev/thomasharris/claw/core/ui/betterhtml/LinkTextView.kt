package dev.thomasharris.claw.core.ui.betterhtml

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView

class LinkTextView : AppCompatTextView {

    // gotta be careful when extending TextView, gotta call everything manually
    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    /**
     *  Just trust a given movement method to handle clicks and such
     *
     *  This is actually basically the same as the super, with editable
     *  stuff removed
     *
     *  I guess I'm not super sure why I'm using a link movement method anymore,
     *  except that it might work better for a11y.  Not quite sure how to check
     *  that though
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val spannable = text as? Spannable
        if ((movementMethod != null || onCheckIsTextEditor()) && isEnabled && spannable != null && layout != null) {
            return movementMethod.onTouchEvent(this, spannable, event)
        }

        return super.onTouchEvent(event)
    }
}