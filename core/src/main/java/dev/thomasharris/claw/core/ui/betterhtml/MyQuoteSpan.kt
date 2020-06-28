package dev.thomasharris.claw.core.ui.betterhtml

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import androidx.annotation.ColorInt

/*
* Copyright (C) 2006 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * A span which styles paragraphs by adding a vertical stripe at the beginning of the text
 * (respecting layout direction).
 *
 *
 * A `QuoteSpan` must be attached from the first character to the last character of a
 * single paragraph, otherwise the span will not be displayed.
 *
 *
 * `QuoteSpans` allow configuring the following elements:
 *
 *  * **color** - the vertical stripe color. By default, the stripe color is 0xff0000ff
 *  * **gap width** - the distance, in pixels, between the stripe and the paragraph.
 * Default value is 2px.
 *  * **stripe width** - the width, in pixels, of the stripe. Default value is
 * 2px.
 *
 * For example, a `QuoteSpan` using the default values can be constructed like this:
 * <pre>`SpannableString string = new SpannableString("Text with quote span on a long line");
 * string.setSpan(new QuoteSpan(), 0, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);`</pre>
 * <img src="{@docRoot}reference/android/images/text/style/defaultquotespan.png"></img>
 * <figcaption>`QuoteSpan` constructed with default values.</figcaption>
 *
 *
 *
 *
 * To construct a `QuoteSpan` with a green stripe, of 20px in width and a gap width of
 * 40px:
 * <pre>`SpannableString string = new SpannableString("Text with quote span on a long line");
 * string.setSpan(new QuoteSpan(Color.GREEN, 20, 40), 0, string.length(),
 * Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);`</pre>
 * <img src="{@docRoot}reference/android/images/text/style/customquotespan.png"></img>
 * <figcaption>Customized `QuoteSpan`.</figcaption>
 */
// adapted from the android source, because the customization was only available on recent android versions
class MyQuoteSpan(
    private val stripeWidth: Int,
    private val indentation: Int,
    @ColorInt
    private val color: Int? = null
) : LeadingMarginSpan {

    private val gapWidth = 16

    override fun getLeadingMargin(first: Boolean): Int {
        return gapWidth + stripeWidth + gapWidth
    }

    override fun drawLeadingMargin(
        c: Canvas, p: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout
    ) {
        val style = p.style
        val color = p.color
        val alpha = p.alpha

        p.style = Paint.Style.FILL
        if (this.color != null)
            p.color = this.color
        else
            p.alpha = (255f * .7f).toInt()

        val xPosition =
            (getLeadingMargin(first) * indentation) + ((getLeadingMargin(first) / 2) - (stripeWidth / 2))
        c.drawRect(
            xPosition.toFloat(),
            top.toFloat(),
            ((xPosition + stripeWidth).toFloat()),
            bottom.toFloat(),
            p
        )
        p.style = style
        p.color = color
        p.alpha = alpha
    }
}