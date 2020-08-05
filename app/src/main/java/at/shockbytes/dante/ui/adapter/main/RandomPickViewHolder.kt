package at.shockbytes.dante.ui.adapter.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class RandomPickViewHolder(
        override val containerView: View
) : BaseAdapter.ViewHolder<BookAdapterEntity>(containerView), LayoutContainer {

    override fun bindToView(content: BookAdapterEntity, position: Int) {
        // TODO
    }

    companion object {

        fun forParent(parent: ViewGroup) : RandomPickViewHolder {
            return RandomPickViewHolder(
                    containerView = LayoutInflater.from(parent.context).inflate(R.layout.item_random_pick, parent, false)
            )
        }
    }
}