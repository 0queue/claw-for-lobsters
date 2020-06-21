package dev.thomasharris.claw.feature.frontpage

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.thomasharris.claw.frontpage.feature.frontpage.R

class DividerViewHolder(private val root: View) : RecyclerView.ViewHolder(root) {

    private val label: TextView = root.findViewById(R.id.item_front_page_divider_text)

    fun bind(item: FrontPageItem.Divider) {
        label.text = root.context.getString(R.string.page_number, item.n)
    }
}