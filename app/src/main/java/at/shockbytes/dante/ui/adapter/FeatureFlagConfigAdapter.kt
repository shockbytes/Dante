package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.flagging.FeatureFlagItem
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feature_flag.*

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

        override fun bindToView(content: FeatureFlagItem, position: Int) {
            with(content) {

                item_feature_flag_txt_title.text = displayName
                item_feature_flag_switch.isChecked = value
            }

            item_feature_flag_root.setOnClickListener {
                item_feature_flag_switch.toggle()
                updateItemState(content)
            }

            item_feature_flag_switch.setOnClickListener {
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