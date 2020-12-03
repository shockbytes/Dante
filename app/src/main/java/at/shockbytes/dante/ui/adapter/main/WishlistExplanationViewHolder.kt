package at.shockbytes.dante.ui.adapter.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_generic_explanation.*
import kotlinx.android.synthetic.main.item_random_pick.*

class WishlistExplanationViewHolder(
    override val containerView: View,
    private val dismissListener: (() -> Unit)?
) : BaseAdapter.ViewHolder<BookAdapterItem>(containerView), LayoutContainer {

    override fun bindToView(content: BookAdapterItem, position: Int) {

        iv_item_generic_explanation_dismiss.setOnClickListener {
            dismissListener?.invoke()
        }

        tv_item_generic_explanation.setText(R.string.wishlist_explanation)

        btn_item_generic_explanation.setVisible(false)

        iv_item_generic_explanation_decoration_start.setImageResource(R.drawable.ic_wishlist)
        iv_item_generic_explanation_decoration_end.setImageResource(R.drawable.ic_wishlist)
    }

    companion object {

        fun forParent(
            parent: ViewGroup,
            dismissListener: (() -> Unit)?
        ): WishlistExplanationViewHolder {
            return WishlistExplanationViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_generic_explanation, parent, false),
                dismissListener
            )
        }
    }
}