package dev.thomasharris.claw.feature.comments.betterhtml

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.text.Layout
import android.text.ParcelableSpan
import android.text.style.LeadingMarginSpan
import android.text.style.QuoteSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px

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
class MyQuoteSpan : LeadingMarginSpan, ParcelableSpan {
    /**
     * Get the color of the quote stripe.
     *
     * @return the color of the quote stripe.
     */
    @get:ColorInt
    @ColorInt
    val color: Int

    /**
     * Get the width of the quote stripe.
     *
     * @return the width of the quote stripe.
     */
    @Px
    val stripeWidth: Int

    /**
     * Get the width of the gap between the stripe and the text.
     *
     * @return the width of the gap between the stripe and the text.
     */
    @Px
    val gapWidth: Int
    /**
     * Creates a [QuoteSpan] based on a color, a stripe width and the width of the gap
     * between the stripe and the text.
     *
     * @param color       the color of the quote stripe.
     * @param stripeWidth the width of the stripe.
     * @param gapWidth    the width of the gap between the stripe and the text.
     */
    /**
     * Creates a [QuoteSpan] with the default values.
     */
    /**
     * Creates a [QuoteSpan] based on a color.
     *
     * @param color the color of the quote stripe.
     */
    @JvmOverloads
    constructor(
        @ColorInt color: Int = STANDARD_COLOR,
        @androidx.annotation.IntRange(from = 0) stripeWidth: Int = STANDARD_STRIPE_WIDTH_PX,
        @androidx.annotation.IntRange(from = 0) gapWidth: Int = STANDARD_GAP_WIDTH_PX
    ) {
        this.color = color
        this.stripeWidth = stripeWidth
        this.gapWidth = gapWidth
    }

    /**
     * Create a [QuoteSpan] from a parcel.
     */
    constructor(src: Parcel) {
        color = src.readInt()
        stripeWidth = src.readInt()
        gapWidth = src.readInt()
    }

    override fun getSpanTypeId(): Int {
        return 100 // original is 9
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcelInternal(dest, flags)
    }

    private fun writeToParcelInternal(dest: Parcel, flags: Int) {
        dest.writeInt(color)
        dest.writeInt(stripeWidth)
        dest.writeInt(gapWidth)
    }

    override fun getLeadingMargin(first: Boolean): Int {
        // TODO leading gapWidth
        return stripeWidth + gapWidth
    }

    override fun drawLeadingMargin(
        c: Canvas, p: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout
    ) {
        val style = p.style
        val color = p.color
        p.style = Paint.Style.FILL
        p.color = this.color
        c.drawRect(x.toFloat(), top.toFloat(), x + dir * stripeWidth.toFloat(), bottom.toFloat(), p)
        p.style = style
        p.color = color
    }

    companion object {
        /**
         * Default stripe width in pixels.
         */
        const val STANDARD_STRIPE_WIDTH_PX = 2

        /**
         * Default gap width in pixels.
         */
        const val STANDARD_GAP_WIDTH_PX = 2

        /**
         * Default color for the quote stripe.
         */
        @ColorInt
        val STANDARD_COLOR = -0xffff01
    }
}