package dev.thomasharris.claw.feature.comments.betterhtml

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
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

fun Element.parse(isOrdered: Boolean = false): SpannableString {
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
                    // TODO just use text color
                    MyQuoteSpan(Color.MAGENTA, 4, 16),
                    0,
                    length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(StyleSpan(Typeface.ITALIC), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
                // TODO change the assumption that pre contains a code?
                setSpan(TypefaceSpan("monospace"), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        "del" -> {
            SpannableString(text()).apply {
                setSpan(StrikethroughSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        "em" -> {
            text().span {
                // TODO look into real vs fake italics
                setSpan(StyleSpan(Typeface.ITALIC), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        "code" -> {
            text().span {
                setSpan(TypefaceSpan("monospace"), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        "strong" -> {
            text().span {
                setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        "ul" -> {
            SpannableString(children().map { it.parse(false) }.concat())
        }
        "li" -> {
            if (isOrdered) {
                SpannableString(parseChildNodes(isOrdered)).apply {
                    setSpan(BulletSpan(16, Color.CYAN), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                // TODO custom bullet span with an indent
                SpannableString(TextUtils.concat(parseChildNodes(isOrdered), "\n")).apply {
                    setSpan(BulletSpan(16), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
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

fun Element.parseChildNodes(isOrdered: Boolean = false): CharSequence {
    return childNodes().map {
        when (it) {
            is Element -> it.parse(isOrdered)
            is TextNode -> it.text()
            else -> ""
        }
    }.concat()
}

fun String.newlines(): String = replace(Regex("\\\\n"), "\n")

fun List<CharSequence>.concat(): CharSequence {
    return TextUtils.concat(*this.toTypedArray())
}

// TODO use androidx.core.text dsl
fun String.span(block: SpannableString.() -> Unit) =
    SpannableString(this).apply(block)
