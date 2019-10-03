package dev.thomasharris.claw.lib.navigator

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler

// copied from the old project, works like a charm
class SlideChangeHandler(duration: Long) : AnimatorChangeHandler(duration, false) {

    constructor() : this(DEFAULT_ANIMATION_DURATION)

    override fun getAnimator(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        toAddedToContainer: Boolean
    ): Animator = AnimatorSet().also { set ->
        (if (isPush) to else from)?.let { v ->
            val x0 = if (isPush) v.width.toFloat() else v.translationX
            val x1 = if (isPush) 0f else v.width.toFloat()
            set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_X, x0, x1))
        }
    }

    override fun resetFromView(from: View) {
        from.translationX = 0f
    }
}
