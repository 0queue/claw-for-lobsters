package dev.thomasharris.claw.feature.comments.betterhtml

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import dev.thomasharris.claw.core.ui.betterlinks.PressableSpan
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * good test area: https://lobste.rs/s/xvqqvy/dynamic_linking.json
 *
 * also good: https://lobste.rs/s/m24zv1/xi_editor_retrospective
 * (andyc going ham with the formatting)
 */
fun String.parseHtml(): CharSequence {
    val parsed = Jsoup.parse(this.replace(Regex("\\\\n"), "\n"))

    val body = parsed.body()

    return body.children().map {
        val res = it.render()

        // generally, elements should care for themselves whether they are paragraphs
        // or not, this is important when, for example, nesting lists.  But the top
        // level should always be made of paragraphs, so if a top level <strong/>
        // for example sneaks through, make it a paragraph
        if (!res.startsWith("\n") && !res.endsWith("\n"))
            res.paragraph()
        else
            res
    }.concat()
}

fun Element.render(): CharSequence {
    return when (tagName()) {
        "p" -> {
            textuals(::identity) { it.render() }.concat().trim().paragraph()
//                .span(BackgroundColorSpan(Color.LTGRAY))
        }
        "a" -> {
            val url = this.attr("abs:href") // may be empty

            text().span(PressableSpan(url))
        }
        "blockquote" -> {
            text().trim().span {
                span(MyQuoteSpan(Color.MAGENTA, 4, 16))
                span(StyleSpan(Typeface.ITALIC))
            }.paragraph()
        }
        "pre" -> {
            textuals(::identity) { it.render() }.concat().span {
                span(MyQuoteSpan(Color.TRANSPARENT, 4, 16))
                span(TypefaceSpan("monospace"))
            }.paragraph()
        }
        "del" -> {
            text().span(StrikethroughSpan())
        }
        "em" -> {
            text().span(StyleSpan(Typeface.ITALIC))
        }
        "code" -> {
            text().span(TypefaceSpan("monospace"))
        }
        "strong" -> {
            text().span(StyleSpan(Typeface.BOLD))
        }
        "ul" -> {
            children().map {
                it.render().span(BulletSpan(32))
            }.concat().trim().paragraph()
        }
        "li" -> {
            textuals(::identity) {
                it.render()
            }.concat().trim() + "\n"
        }
        "hr" -> {
            "-".span(HrSpan()).paragraph()
        }
        else -> {
            text().span(ForegroundColorSpan(Color.CYAN))
        }
    }
}

fun List<CharSequence>.concat(): CharSequence = TextUtils.concat(*this.toTypedArray())
operator fun CharSequence.plus(that: CharSequence): CharSequence = TextUtils.concat(this, that)

fun Node.textuals(
    textBlock: (String) -> CharSequence,
    elementBlock: (Element) -> CharSequence
): List<CharSequence> = childNodes().mapNotNull {
    when (it) {
        is Element -> elementBlock(it)
        is TextNode -> textBlock(it.text())
        else -> null
    }
}

fun CharSequence.span(block: SpannableString.() -> Unit) =
    SpannableString(this).apply(block)

fun CharSequence.span(span: Any): SpannableString = SpannableString(this).apply { span(span) }

fun SpannableString.span(span: Any) = setSpan(span, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

fun CharSequence.paragraph(): CharSequence = listOf("\n", this, "\n").concat()

fun identity(s: String): CharSequence = s