package dev.thomasharris.claw.core.ext

import android.view.View

fun View.fade(fadeIn: Boolean) {

    if (visibility == View.GONE && !fadeIn)
        return

    if (fadeIn)
        visibility = View.VISIBLE

    animate().apply {
        cancel()
        duration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        alpha(if (fadeIn) 1f else 0f)
        if (fadeIn)
            setListener(null)
        else
            withEndAction {
                visibility = View.GONE
            }
    }
}
