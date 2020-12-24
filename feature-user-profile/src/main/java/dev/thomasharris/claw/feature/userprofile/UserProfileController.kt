package dev.thomasharris.claw.feature.userprofile

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import dev.thomasharris.betterhtml.PressableLinkMovementMethod
import dev.thomasharris.betterhtml.PressableSpan
import dev.thomasharris.betterhtml.fromHtml
import dev.thomasharris.claw.core.HasBinding
import dev.thomasharris.claw.core.R
import dev.thomasharris.claw.core.ext.dipToPx
import dev.thomasharris.claw.core.ext.fade
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ext.postedAgo
import dev.thomasharris.claw.core.ext.toString
import dev.thomasharris.claw.core.ui.TagSpan
import dev.thomasharris.claw.core.ui.ViewLifecycleController
import dev.thomasharris.claw.feature.userprofile.databinding.ControllerUserProfileBinding
import dev.thomasharris.claw.feature.userprofile.di.DaggerUserProfileComponent
import dev.thomasharris.claw.feature.userprofile.di.UserProfileComponent
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.back
import dev.thomasharris.claw.lib.navigator.goto
import dev.thomasharris.claw.lib.swipeback.SwipeBackTouchListener
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserProfileController(
    args: Bundle,
) : ViewLifecycleController(args), HasBinding<ControllerUserProfileBinding> {

    private val component by getComponent<UserProfileComponent> {
        DaggerUserProfileComponent.builder()
            .singletonComponent(it)
            .build()
    }

    override var binding: ControllerUserProfileBinding? = null

    private var preDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    private val username = args.getString("username")!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?,
    ): View {
        binding = ControllerUserProfileBinding.inflate(inflater, container, false).apply {
            root.listener = SwipeBackTouchListener(root.context) {
                back()
            }

            // only elevate if in movement, a static elevation in the layout
            // means layers of UserProfile don't have elevation over each other!
            preDrawListener = ViewTreeObserver.OnPreDrawListener {
                root.elevation = if (root.translationX != 0f) 4f.dipToPx(root.context) else 0f
                true
            }
            root.viewTreeObserver.addOnPreDrawListener(preDrawListener)

            root.setOnApplyWindowInsetsListener { v, insets ->
                v.onApplyWindowInsets(insets)
                insets
            }

            with(userProfileToolbar) {
                setNavigationOnClickListener { back() }
                title = "User Profile"
            }

            nestedScrollView.setOnScrollChangeListener { v, _, _, _, _ ->
                userProfileAppBarLayout.isSelected = v.canScrollVertically(-1)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                component.userRepository.refresh(username)
                component.userRepository.latestUser(username).collect { user ->
                    if (user != null) {
                        usernameText.text = username

                        joinedText.text =
                            user.createdAt.postedAgo().toString(root.context).let { ago ->
                                user.invitedByUser?.let { inviter ->
                                    @Suppress("BlockingMethodInNonBlockingContext") // ??
                                    SpannableStringBuilder().apply {
                                        append("$ago invited by ")
                                        append(
                                            user.invitedByUser.toString(),
                                            PressableSpan(inviter),
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                                        )
                                    }
                                } ?: ago
                            }
                        joinedText.movementMethod = PressableLinkMovementMethod {
                            // the link is to another profile only
                            if (it != null && it.isNotBlank())
                                goto(Destination.UserProfile(it))
                        }

                        privilegesTextLabel.visibility =
                            if (user.isAdmin || user.isModerator) View.VISIBLE else View.GONE
                        privilegesText.visibility =
                            if (user.isAdmin || user.isModerator) View.VISIBLE else View.GONE
                        @Suppress("BlockingMethodInNonBlockingContext")
                        privilegesText.text = SpannableStringBuilder().apply {
                            /**
                             * So, if a replacement span covers the whole text, the span has to
                             * set the height somehow, using the font metrics in getSize.  If it
                             * doesn't, it completely breaks the whole ConstraintLayout, which is
                             * impressive to say the least.
                             *
                             * As I have neither the time nor energy to dive back in the weird world
                             * of span implementations, instead use a space to enable whatever
                             * automatic height was already being used.
                             */
                            if (user.isAdmin || user.isModerator)
                                append(" ")

                            if (user.isAdmin)
                                append(
                                    "admin",
                                    TagSpan(
                                        backgroundColor = root.context.color(R.attr.colorTagBackgroundIsAdmin),
                                        borderColor = root.context.color(R.attr.colorTagBorderIsAdmin)
                                    ),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )

                            if (user.isAdmin && user.isModerator)
                                append(" ")

                            if (user.isModerator)
                                append(
                                    "moderator",
                                    TagSpan(
                                        backgroundColor = root.context.color(R.attr.colorTagBackground),
                                        borderColor = root.context.color(R.attr.colorTagBorder)
                                    ),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                        }

                        karmaText.text = user.karma.toString()

                        val linkMovementMethod = PressableLinkMovementMethod {
                            if (it != null) goto(Destination.WebPage(it))
                        }

                        githubTextLabel.visibility =
                            if (user.githubUsername != null) View.VISIBLE else View.GONE
                        githubText.visibility =
                            if (user.githubUsername != null) View.VISIBLE else View.GONE

                        if (user.githubUsername != null) {
                            githubText.movementMethod = linkMovementMethod
                            @SuppressLint("SetTextI18n") // it's just a link so it's fine
                            githubText.text =
                                """<a href="https://github.com/${user.githubUsername}">https://github.com/${user.githubUsername}</a> """
                                    .fromHtml(true) { it.dipToPx(root.context) }
                        }

                        twitterTextLabel.visibility =
                            if (user.twitterUsername != null) View.VISIBLE else View.GONE
                        twitterText.visibility =
                            if (user.twitterUsername != null) View.VISIBLE else View.GONE
                        if (user.twitterUsername != null) {
                            twitterText.movementMethod = linkMovementMethod
                            @SuppressLint("SetTextI18n") // it's just a link so it's fine
                            twitterText.text =
                                """<a href="https://twitter.com/${user.twitterUsername}">@${user.twitterUsername}</a> """
                                    .fromHtml(true) { it.dipToPx(root.context) }
                        }

                        aboutText.text = if (user.about.isNotBlank())
                            component.renderMarkdownUseCase(user.about) { it.dipToPx(root.context) }
                        else
                            "<em>A mystery...</em>"
                                .fromHtml(true) { it.dipToPx(root.context) }
                        aboutText.movementMethod = linkMovementMethod

                        avatar.load("https://lobste.rs/${user.avatarShortUrl}") {
                            crossfade(true)
                            placeholder(R.drawable.ic_person_black_24dp)
                            transformations(CircleCropTransformation())
                        }
                    }

                    constraintLayout.fade(user != null)
                }
            }
        }

        return requireBinding().root
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        if (preDrawListener != null)
            binding?.root?.viewTreeObserver?.removeOnPreDrawListener(preDrawListener)
    }
}

@ColorInt
private fun Context.color(@AttrRes attr: Int): Int = TypedValue().run {
    theme.resolveAttribute(attr, this, true)
    data
}
