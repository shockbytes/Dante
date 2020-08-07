package at.shockbytes.dante.ui.adapter.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_random_pick.*

class RandomPickViewHolder(
        override val containerView: View,
        private val callback: RandomPickCallback
) : BaseAdapter.ViewHolder<BookAdapterEntity>(containerView), LayoutContainer {

    override fun bindToView(content: BookAdapterEntity, position: Int) {

        btn_item_random_pick.setOnClickListener {
            callback.onRandomPickClicked()
        }

        iv_item_random_pick_dismiss.setOnClickListener {
            callback.onDismiss()
        }
    }

    companion object {

        fun forParent(
                parent: ViewGroup,
                callback: RandomPickCallback
        ) : RandomPickViewHolder {
            return RandomPickViewHolder(
                    containerView = LayoutInflater.from(parent.context).inflate(R.layout.item_random_pick, parent, false),
                    callback = callback
            )
        }
    }
}