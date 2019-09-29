package dev.thomasharris.claw.core.ext

import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import dev.thomasharris.claw.core.R

fun View.fade(fadeIn: Boolean) {
    if (visibility == View.GONE && !fadeIn)
        return

    if (fadeIn)
        visibility = View.VISIBLE
    animate().apply {
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

fun Toolbar.setScrollEnabled(enabled: Boolean) {
    val origScrollParams: AppBarLayout.LayoutParams =
        (getTag(R.id.TOOLBAR_ORIGINAL_SCROLL_FLAGS_KEY) as? AppBarLayout.LayoutParams)
            ?: (layoutParams as AppBarLayout.LayoutParams).also {
                setTag(R.id.TOOLBAR_ORIGINAL_SCROLL_FLAGS_KEY, it)
            }

    val noScrollParams = AppBarLayout.LayoutParams(origScrollParams).apply {
        scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
    }

    layoutParams = if (enabled) origScrollParams else noScrollParams
}