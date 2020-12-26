package dev.thomasharris.claw.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.thomasharris.claw.core.HasBinding
import dev.thomasharris.claw.core.R
import dev.thomasharris.claw.core.databinding.ControllerStoryAdditionalActionsBinding
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.back
import dev.thomasharris.claw.lib.navigator.goto

@Suppress("unused")
class StoryAdditionalActionsController(
    args: Bundle,
) : ViewLifecycleController(args), HasBinding<ControllerStoryAdditionalActionsBinding> {
    override var binding: ControllerStoryAdditionalActionsBinding? = null

    private val author = args.getString("author")!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?,
    ): View {
        binding = ControllerStoryAdditionalActionsBinding.inflate(inflater, container, false).apply {
            scrim.setOnClickListener {
                back()
            }

            dialog.setOnClickListener { }

            viewProfileButton.text =
                resources!!.getString(R.string.story_additional_action_view_profile, author)

            viewProfileButton.setOnClickListener {
                back()
                goto(Destination.UserProfile(author))
            }
        }

        return requireBinding().root
    }
}
