package dev.thomasharris.claw.feature.webpage

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.bluelinelabs.conductor.Controller
import dev.thomasharris.claw.lib.navigator.back

private const val REQUEST_CODE = 102019

@Suppress("unused")
class WebPageController(args: Bundle) : Controller(args) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedStateBundle: Bundle?
    ): View = inflater.inflate(R.layout.web_page, container, false)

    override fun onAttach(view: View) {
        super.onAttach(view)

        val url = args.getString("webPageUrl")!!

        // TODO eventually fallback to web view
        CustomTabsIntent.Builder().apply {
            activity?.bitmapFromVector(R.drawable.ic_arrow_back_black_24dp)?.let {
                setCloseButtonIcon(it)
            }

            setShowTitle(true)

            activity?.let {
                setStartAnimations(it, R.anim.slide_in_from_right, R.anim.nothing)
                setExitAnimations(it, R.anim.nothing, R.anim.slide_out_to_right)
                // closest thing to turning on dark mode as far as I can tell
                setDefaultColorSchemeParams(CustomTabColorSchemeParams.Builder().apply {
                    setToolbarColor(it.getColorAttr(R.attr.colorSurface))
                }.build())
            }
        }.build().apply {
            intent.data = Uri.parse(url)
            startActivityForResult(intent, REQUEST_CODE, startAnimationBundle)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE)
            back()
    }
}

fun Context.bitmapFromVector(drawableId: Int): Bitmap? {
    val drawable = ContextCompat.getDrawable(this, drawableId) ?: return null
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun Context.getColorAttr(@AttrRes attr: Int): Int {
    TypedValue().let {
        theme.resolveAttribute(attr, it, true)
        return it.data
    }
}