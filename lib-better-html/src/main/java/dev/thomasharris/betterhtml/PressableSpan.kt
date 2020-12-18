package dev.thomasharris.betterhtml

import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

class PressableSpan(val url: String) : ClickableSpan() {

    var isPressed = false

    // for sale, onClick method, never called
    override fun onClick(widget: View) = Unit

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.bgColor = if (isPressed) with(ds.linkColor) {
            Color.argb((alpha * .25).toInt(), red, green, blue)
        } else ds.bgColor
    }
}
