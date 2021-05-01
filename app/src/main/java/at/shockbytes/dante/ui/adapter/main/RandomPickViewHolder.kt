package at.shockbytes.dante.ui.adapter.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ItemRandomPickBinding
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class RandomPickViewHolder(
    override val containerView: View,
    private val callback: RandomPickCallback?
) : BaseAdapter.ViewHolder<BookAdapterItem>(containerView), LayoutContainer {

    private val vb = ItemRandomPickBinding.bind(containerView)

    override fun bindToView(content: BookAdapterItem, position: Int) {

        vb.btnItemRandomPick.setOnClickListener {
            callback?.onRandomPickClicked()
        }

        vb.ivItemRandomPickDismiss.setOnClickListener {
            callback?.onDismiss()
        }
    }

    companion object {

        fun forParent(
            parent: ViewGroup,
            callback: RandomPickCallback?
        ): RandomPickViewHolder {
            return RandomPickViewHolder(
                containerView = LayoutInflater.from(parent.context).inflate(R.layout.item_random_pick, parent, false),
                callback = callback
            )
        }
    }
}