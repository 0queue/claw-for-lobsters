package dev.thomasharris.claw.feature.comments

import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast

class PressableSpan(val url: String) : ClickableSpan() {

    var isPressed = false

    override fun onClick(widget: View) {
        // TODO maybe??
        // oh yeah I never call this in my movement method
        Toast.makeText(widget.context, "CLICK $url", Toast.LENGTH_SHORT).show()
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.bgColor = if (isPressed) Color.GREEN else ds.bgColor
    }
}