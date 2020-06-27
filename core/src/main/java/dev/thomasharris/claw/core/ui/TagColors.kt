package dev.thomasharris.claw.core.ui

import android.content.Context
import android.util.TypedValue
import dev.thomasharris.claw.core.R

// from https://github.com/lobsters/lobsters/blob/master/app/assets/stylesheets/local/lobsters.css
val showAskAnnounceInterview = setOf(
    "show",
    "ask",
    "announce",
    "interview"
)

val meta = setOf("meta")

// calculated with raku:
// ```
// use WWW;
// ((jget "https://lobste.rs/tags.json").grep: { .<is_media> }).map: { .<tag> }
// ```
val isMedia = setOf(
    "ask",
    "audio",
    "pdf",
    "show",
    "slides",
    "transcript",
    "video"
)

private fun Context.resolveColor(attr: () -> Int) = TypedValue().run {
    theme.resolveAttribute(attr(), this, true)
    data
}

fun Context.tagBorder(tag: String) = resolveColor {
    when {
        showAskAnnounceInterview.contains(tag) -> R.attr.colorTagBorderShowAskAnnounceInterview
        meta.contains(tag) -> R.attr.colorTagBorderMeta
        isMedia.contains(tag) -> R.attr.colorTagBorderMedia
        else -> R.attr.colorTagBorder
    }
}

fun Context.tagBackground(tag: String) = resolveColor {
    when {
        showAskAnnounceInterview.contains(tag) -> R.attr.colorTagBackgroundShowAskAnnounceInterview
        meta.contains(tag) -> R.attr.colorTagBackgroundMeta
        isMedia.contains(tag) -> R.attr.colorTagBackgroundMedia
        else -> R.attr.colorTagBackground
    }
}
