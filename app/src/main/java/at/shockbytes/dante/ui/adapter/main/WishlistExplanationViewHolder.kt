package at.shockbytes.dante.ui.adapter.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ItemGenericExplanationBinding
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class WishlistExplanationViewHolder(
    override val containerView: View,
    private val dismissListener: (() -> Unit)?
) : BaseAdapter.ViewHolder<BookAdapterItem>(containerView), LayoutContainer {

    private val vb = ItemGenericExplanationBinding.bind(containerView)

    override fun bindToView(content: BookAdapterItem, position: Int) {

        vb.ivItemGenericExplanationDismiss.setOnClickListener {
            dismissListener?.invoke()
        }

        vb.tvItemGenericExplanation.setText(R.string.wishlist_explanation)

        vb.btnItemGenericExplanation.setVisible(false)

        vb.ivItemGenericExplanationDecorationStart.setImageResource(R.drawable.ic_wishlist)
        vb.ivItemGenericExplanationDecorationEnd.setImageResource(R.drawable.ic_wishlist)
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