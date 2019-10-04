package dev.thomasharris.claw.feature.comments

import android.animation.ObjectAnimator
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max


private const val TRANSLATION_SCALAR = 1.75f
private const val THRESHOLD_PERCENT = .30f
private const val MAX_SWIPE_ANGLE = 15f // degrees

class CommentsTouchListener(context: Context, private val onThreshold: () -> Unit) :
    TouchInterceptingCoordinatorLayout.Listener {

    private var startX = 0f
    private var startY = 0f
    private var startTranslationX = 0f // for "catching" the recyclerview
    private var objectAnimator: ObjectAnimator? = null

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {


                if (e1 == null || e2 == null)
                    return false

                val angle = atan2(e1.rawY - e2.rawY, e2.rawX - e1.rawX) * (180f / PI)
                if (abs(angle) < MAX_SWIPE_ANGLE) {
                    onThreshold()
                    return true
                }

                return false
            }
        })


    override fun onInterceptTouchEvent(view: View, ev: MotionEvent) = when (ev.action) {
        MotionEvent.ACTION_DOWN -> {
            startX = ev.rawX
            startY = ev.rawY
            startTranslationX = view.translationX
            objectAnimator?.cancel()
            gestureDetector.onTouchEvent(ev)
            false
        }

        MotionEvent.ACTION_MOVE -> {
            val angle = atan2(startY - ev.rawY, ev.rawX - startX) * (180f / PI)
            val res = (ev.rawX > startX && abs(angle) < MAX_SWIPE_ANGLE)
            res
        }
        else -> false
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event))
            return true

        // ACTION_DOWN _not_ intercepted in case this isn't a swipe
        // and it is probably consumed by something else
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                (startTranslationX + (event.rawX - startX) / TRANSLATION_SCALAR).let {
                    view.translationX = max(it, 0f)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {

                // This isn't specifically intercepted in onInterceptTouchEvent
                // but we receive it anyways, which is nice
                //
                // presumably because no one else consumes it?

                if (view.translationX > view.width * THRESHOLD_PERCENT)
                    onThreshold()
                else if (view.translationX > 0) objectAnimator =
                    ObjectAnimator.ofFloat(view, "translationX", 0f).apply {
                        interpolator = FastOutSlowInInterpolator()
                        duration =
                            view.context.resources.getInteger(android.R.integer.config_mediumAnimTime)
                                .toLong()
                        start()
                    }

                return true
            }
            else -> return false
        }
    }

}