package dev.thomasharris.claw.feature.frontpage

import androidx.recyclerview.widget.RecyclerView
import dev.thomasharris.claw.frontpage.feature.frontpage.R
import dev.thomasharris.claw.frontpage.feature.frontpage.databinding.ItemFrontPageDividerBinding

class DividerViewHolder(
    private val binding: ItemFrontPageDividerBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: FrontPageItem.Divider) {
        binding.itemFrontPageDividerText.text =
            binding.root.context.getString(R.string.page_number, item.n)
    }
}