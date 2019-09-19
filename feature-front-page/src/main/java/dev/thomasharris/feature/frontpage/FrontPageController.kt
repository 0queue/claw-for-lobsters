package dev.thomasharris.feature.frontpage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.bluelinelabs.conductor.Controller

class FrontPageController : Controller() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.front_page, container, false)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        view.findViewById<Toolbar>(R.id.front_page_toolbar).title = "Lobste.rs"
    }
}