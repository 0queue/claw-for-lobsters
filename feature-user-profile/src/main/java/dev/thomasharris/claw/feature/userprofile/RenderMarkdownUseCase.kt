package dev.thomasharris.claw.feature.userprofile

import dev.thomasharris.betterhtml.fromHtml
import dev.thomasharris.claw.core.di.FeatureScope
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import javax.inject.Inject

@FeatureScope
class RenderMarkdownUseCase @Inject constructor(
    private val parser: Parser,
    private val htmlRenderer: HtmlRenderer,
) {
    operator fun invoke(markdown: String, dipToPx: (Float) -> Float): CharSequence =
        markdown
            .let(parser::parse)
            .let(htmlRenderer::render)
            .fromHtml(dipToPx = dipToPx)
            .trim()
}
