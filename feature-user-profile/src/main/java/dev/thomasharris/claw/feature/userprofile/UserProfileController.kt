package dev.thomasharris.claw.feature.userprofile

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

            root.setOnApplyWindowInsetsListener { v, insets ->
                v.onApplyWindowInsets(insets)
                insets
            }

            with(userProfileToolbar) {
                setNavigationOnClickListener { back() }
                title = "User Profile"
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
                                        append(user.invitedByUser.toString())
                                        setSpan(
                                            PressableSpan(inviter),
                                            this.length - inviter.length,
                                            this.length,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                        )
                                    }
                                } ?: ago
                            }
                        joinedText.movementMethod = PressableLinkMovementMethod {
                            // the link is to another profile only
                            if (it != null && it.isNotBlank())
                                goto(Destination.UserProfile(it))
                        }

                        // TODO add privileges (isAdmin, isModerator) as chips
                        //  with label Privileges here

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

                        // TODO the damn about text isn't html, it's still markdown...
                        aboutText.text = user.about.ifEmpty {
                            "<em>A mystery...</em>"
                                .fromHtml(true) { it.dipToPx(root.context) }
                        }

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
}
