package dev.thomasharris.claw.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.thomasharris.claw.core.HasBinding
import dev.thomasharris.claw.core.databinding.StoryAdditionalActionsBinding
import dev.thomasharris.claw.lib.navigator.back

class StoryAdditionalActionsController(
    args: Bundle,
) : ViewLifecycleController(args), HasBinding<StoryAdditionalActionsBinding> {
    override var binding: StoryAdditionalActionsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?,
    ): View {
        binding = StoryAdditionalActionsBinding.inflate(inflater, container, false).apply {
            title.text = "Dialog test"

            scrim.setOnClickListener {
                back()
            }
        }

        return requireBinding().root
    }
}
