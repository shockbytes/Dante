package at.shockbytes.dante.ui.custom.colorpicker

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import at.shockbytes.dante.databinding.ColorPickerViewBinding
import at.shockbytes.dante.util.layoutInflater
import at.shockbytes.util.adapter.BaseAdapter

class ColorPickerView(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val vb = ColorPickerViewBinding.inflate(context.layoutInflater(), this, true)

    var colors: List<ColorPickerItem> = listOf()
        set(value) {
            field = value
            updateRecyclerViewAdapter(value)
        }

    var columns: Int = 5

    private val colorItemAdapter: ColorItemAdapter by lazy {
        ColorItemAdapter(context, object : BaseAdapter.OnItemClickListener<ColorPickerItem> {
            override fun onItemClick(content: ColorPickerItem, position: Int, v: View) {
                highlightItem(content)
            }
        })
    }

    private fun highlightItem(content: ColorPickerItem) {
        colors = colors.map { c ->
            val isItemSelected = c.colorRes == content.colorRes
            c.copy(isSelected = isItemSelected)
        }
    }

    val selectedItem: ColorPickerItem?
        get() = colors.find { it.isSelected }

    fun initialize(colorItems: List<ColorPickerItem>) {
        vb.rvColorPicker.apply {
            layoutManager = GridLayoutManager(context, columns)
            adapter = colorItemAdapter
        }

        this.colors = colorItems
    }

    private fun updateRecyclerViewAdapter(values: List<ColorPickerItem>) {
        colorItemAdapter.updateData(values)
    }
}