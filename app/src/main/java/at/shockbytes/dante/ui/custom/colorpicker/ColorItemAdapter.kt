package at.shockbytes.dante.ui.custom.colorpicker

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import at.shockbytes.dante.R
import at.shockbytes.dante.util.ColorUtils
import at.shockbytes.dante.util.isNightModeEnabled
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.color_picker_item.*

class ColorItemAdapter(
    context: Context,
    onItemClickListener: OnItemClickListener<ColorPickerItem>
) : BaseAdapter<ColorPickerItem>(context, onItemClickListener) {

    private val isNightModeEnabled = context.isNightModeEnabled()

    fun updateData(colors: List<ColorPickerItem>) {
        val diffResult = DiffUtil.calculateDiff(ColorItemDiffUtilCallback(data, colors))

        data.clear()
        data.addAll(colors)

        diffResult.dispatchUpdatesTo(this)
    }

    class ColorItemDiffUtilCallback(
        private val oldList: List<ColorPickerItem>,
        private val newList: List<ColorPickerItem>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].colorRes == newList[newItemPosition].colorRes
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ColorPickerItem> {
        return ColorItemViewHolder(inflater.inflate(R.layout.color_picker_item, parent, false))
    }

    inner class ColorItemViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<ColorPickerItem>(containerView), LayoutContainer {

        override fun bindToView(content: ColorPickerItem, position: Int) {
            with(content) {

                val chipColor = if (isNightModeEnabled) {
                    ColorUtils.desaturateAndDevalue(ContextCompat.getColor(context, colorRes), by = 0.25f)
                } else {
                    ContextCompat.getColor(context, colorRes)
                }

                view_color_picker_item.setCardBackgroundColor(chipColor)
                iv_color_picker_item_selected.setVisible(isSelected)
            }
        }
    }
}