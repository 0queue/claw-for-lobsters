package dev.thomasharris.claw.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dev.thomasharris.claw.core.HasBinding
import dev.thomasharris.claw.core.R
import dev.thomasharris.claw.core.databinding.StoryAdditionalActionsBinding
import dev.thomasharris.claw.lib.navigator.back

class StoryAdditionalActionsController(
    args: Bundle,
) : ViewLifecycleController(args), HasBinding<StoryAdditionalActionsBinding> {
    override var binding: StoryAdditionalActionsBinding? = null

    private val author = args.getString("author")!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?,
    ): View {
        binding = StoryAdditionalActionsBinding.inflate(inflater, container, false).apply {
            scrim.setOnClickListener {
                back()
            }

            dialog.setOnClickListener { }

            viewProfileButton.text =
                resources!!.getString(R.string.story_additional_action_view_profile, author)

            viewProfileButton.setOnClickListener {
                Toast.makeText(activity, "asdf", Toast.LENGTH_SHORT).show()
            }
        }

        return requireBinding().root
    }
}
