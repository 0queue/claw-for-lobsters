package dev.thomasharris.claw.core.ui.betterhtml

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

class PressableSpan(val url: String) : ClickableSpan() {

    var isPressed = false

    // for sale, onClick method, never called
    override fun onClick(widget: View) = Unit

    // not sure that this is ever called either tbh
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.bgColor = if (isPressed) with(ds.linkColor) {
            Color.argb((alpha * .25).toInt(), red, green, blue)
        } else ds.bgColor
    }
}

fun Spanned.replaceUrlSpans(): SpannableString = SpannableString(this).apply {
    getSpans(0, length, URLSpan::class.java).forEach {
        val start = getSpanStart(it)
        val end = getSpanEnd(it)
        removeSpan(it)
        setSpan(
            PressableSpan(it.url), start, end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}