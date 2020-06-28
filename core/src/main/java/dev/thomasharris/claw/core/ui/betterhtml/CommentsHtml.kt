package dev.thomasharris.claw.core.ui.betterhtml

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
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
fun String.fromHtml(dipToPx: (Float) -> Float = { it }): CharSequence {
    val parsed = Jsoup.parse(this.replace(Regex("\\\\n"), "\n"))

    val body = parsed.body()

    return body.children().map {
        val res = it.render(dipToPx)

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

fun Element.render(dipToPx: (Float) -> Float, indentation: Int = 0): CharSequence {
    return when (tagName()) {
        "p" -> {
            textuals(::identity) { it.render(dipToPx, indentation) }.concat().trim().paragraph()
//                .span(BackgroundColorSpan(Color.LTGRAY))
        }
        "a" -> {
            val url = this.attr("abs:href") // may be empty

            text().span(
                PressableSpan(
                    url
                )
            )
        }
        "blockquote" -> {
            textuals(::identity) { it.render(dipToPx, indentation + 1) }.concat().trim().span {
                span(
                    MyQuoteSpan(
                        stripeWidth = dipToPx(2f).toInt(),
                        indentation = indentation
                    )
                )
                span(StyleSpan(Typeface.ITALIC))
            }.paragraph()
        }
        "pre" -> {
            textuals(::identity) { it.render(dipToPx, indentation) }.concat().span {
                span(
                    MyQuoteSpan(
                        stripeWidth = dipToPx(2f).toInt(),
                        indentation = indentation,
                        color = Color.TRANSPARENT
                    )
                )
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
                it.render(dipToPx, indentation).span(MyBulletSpan(indentation))
            }.concat().trim().paragraph()
        }
        "li" -> {
            textuals(::identity) {
                it.render(dipToPx, indentation + 1)
            }.concat().trim() + "\n"
        }
        "hr" -> {
            "-".span(HrSpan(dipToPx(2f).toInt())).paragraph()
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