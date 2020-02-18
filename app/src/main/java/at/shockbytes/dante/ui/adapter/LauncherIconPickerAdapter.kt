package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.ui.viewmodel.LauncherIconPickerViewModel
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class LauncherIconPickerAdapter(
    context: Context
) : BaseAdapter<LauncherIconPickerViewModel.LauncherIconItem>(context) {

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

            }
        }
    }
}