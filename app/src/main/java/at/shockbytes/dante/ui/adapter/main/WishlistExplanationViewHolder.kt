package at.shockbytes.dante.ui.adapter.main

import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ItemGenericExplanationBinding
import at.shockbytes.dante.util.layoutInflater
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class WishlistExplanationViewHolder(
    val vb: ItemGenericExplanationBinding,
    private val dismissListener: (() -> Unit)?
) : BaseAdapter.ViewHolder<BookAdapterItem>(vb.root) {

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
                ItemGenericExplanationBinding.inflate(parent.context.layoutInflater(), parent, false),
                dismissListener
            )
        }
    }
}