package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ItemFeatureFlagBinding
import at.shockbytes.dante.flagging.FeatureFlagItem
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class FeatureFlagConfigAdapter(
    context: Context,
    private val onItemChangedListener: ((item: FeatureFlagItem) -> Unit)
) : BaseAdapter<FeatureFlagItem>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_feature_flag, parent, false))
    }

    inner class ViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<FeatureFlagItem>(containerView), LayoutContainer {

        private val vb = ItemFeatureFlagBinding.bind(containerView)

        override fun bindToView(content: FeatureFlagItem, position: Int) {
            with(content) {

                vb.itemFeatureFlagTxtTitle.text = displayName
                vb.itemFeatureFlagSwitch.isChecked = value
            }

            vb.itemFeatureFlagRoot.setOnClickListener {
                vb.itemFeatureFlagSwitch.toggle()
                updateItemState(content)
            }

            vb.itemFeatureFlagSwitch.setOnClickListener {
                updateItemState(content)
            }
        }

        private fun updateItemState(item: FeatureFlagItem) {

            val position = getLocation(item)
            if (position > -1) {
                val updatedItem = data[position].copy(value = !data[position].value)
                data[position] = updatedItem
                onItemChangedListener.invoke(updatedItem)
                notifyItemChanged(position)
            }
        }
    }
}