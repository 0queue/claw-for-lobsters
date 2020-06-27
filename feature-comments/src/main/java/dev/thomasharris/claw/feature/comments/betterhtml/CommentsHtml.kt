package dev.thomasharris.claw.feature.comments.betterhtml

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.TypefaceSpan
import dev.thomasharris.claw.core.ui.betterlinks.PressableSpan
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

/**
 * good test area: https://lobste.rs/s/xvqqvy/dynamic_linking.json
 */
fun String.parseHtml(): CharSequence {
    val parsed = Jsoup.parse(this.newlines())

    /**
     * General stragegy for parsing: start with body, depth first down to text nodes, concat together
     */

    val body = parsed.body()


    return TextUtils.concat(*body.children().map { TextUtils.concat(it.parse(), "\n\n") }
        .toTypedArray())
}

fun Element.parse(): SpannableString {
    return when (tagName()) {
        "p" -> {

            val content = childNodes().map {
                when (it) {
                    is TextNode -> {
                        it.text()
                    }
                    is Element -> {
                        it.parse()
                    }
                    else -> ""
                }
            }.concat()

            SpannableString(content.trim()).apply {
//                setSpan(BackgroundColorSpan(Color.LTGRAY), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        "a" -> {
            val url = this.attr("abs:href")// may be empty

            SpannableString(text().trim()).apply {
                setSpan(PressableSpan(url), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        "blockquote" -> {
            SpannableString(text().trim()).apply {
                setSpan(
                    MyQuoteSpan(Color.MAGENTA, 4, 16),
                    0,
                    length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(TypefaceSpan("serif"), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        "pre" -> {
            SpannableString(text()).apply {
                setSpan(
                    MyQuoteSpan(Color.TRANSPARENT, 4, 16),
                    0,
                    length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(TypefaceSpan("monospace"), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        "del" -> {
            SpannableString(text()).apply {
                setSpan(StrikethroughSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        else -> {
            SpannableString(text()).apply {
                setSpan(
                    ForegroundColorSpan(Color.CYAN),
                    0,
                    length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
}

fun String.newlines(): String = replace(Regex("\\\\n"), "\n")

fun List<CharSequence>.concat(): CharSequence {
    return TextUtils.concat(*this.toTypedArray())
}