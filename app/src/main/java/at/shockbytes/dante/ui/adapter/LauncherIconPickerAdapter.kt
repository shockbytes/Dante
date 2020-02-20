package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.ui.viewmodel.LauncherIconPickerViewModel
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_launcher_icon_item.*

class LauncherIconPickerAdapter(
    context: Context,
    onItemClickListener: OnItemClickListener<LauncherIconPickerViewModel.LauncherIconItem>
) : BaseAdapter<LauncherIconPickerViewModel.LauncherIconItem>(context, onItemClickListener) {

    fun updateData(newContent: List<LauncherIconPickerViewModel.LauncherIconItem>) {

        data.clear()
        data.addAll(newContent)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<LauncherIconPickerViewModel.LauncherIconItem> {
        return LauncherIconViewHolder(inflater.inflate(R.layout.item_launcher_icon_item, parent, false))
    }

    inner class LauncherIconViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<LauncherIconPickerViewModel.LauncherIconItem>(containerView), LayoutContainer {
        override fun bindToView(content: LauncherIconPickerViewModel.LauncherIconItem, position: Int) {
            with(content) {

                iv_item_launcher_icon_item.setImageResource(iconLauncherIconState.icon)
                tv_item_launcher_icon_item.text = iconLauncherIconState.title

                iv_item_launcher_icon_item_checked.setVisible(isSelected)
            }
        }
    }
}