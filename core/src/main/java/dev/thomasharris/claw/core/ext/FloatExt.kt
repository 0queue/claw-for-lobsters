package dev.thomasharris.claw.core.ext

import android.content.Context
import android.util.TypedValue

fun Float.dipToPx(context: Context) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)
