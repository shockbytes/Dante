package at.shockbytes.dante.ui.adapter.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_random_pick.*

class WishlistExplanationViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<BookAdapterItem>(containerView), LayoutContainer {

    override fun bindToView(content: BookAdapterItem, position: Int) {
    }

    companion object {

        fun forParent(parent: ViewGroup): WishlistExplanationViewHolder {
            return WishlistExplanationViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_generic_explanation, parent, false)
            )
        }
    }
}