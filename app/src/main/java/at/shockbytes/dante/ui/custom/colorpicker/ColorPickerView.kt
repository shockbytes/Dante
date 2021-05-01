package at.shockbytes.dante.ui.custom.colorpicker

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import at.shockbytes.dante.databinding.ColorPickerViewBinding
import at.shockbytes.dante.util.layoutInflater
import at.shockbytes.util.adapter.BaseAdapter

class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val vb = ColorPickerViewBinding.inflate(context.layoutInflater(), this, true)

    var colors: List<ColorPickerItem> = listOf()
        set(value) {
            field = value
            updateRecyclerViewAdapter(value)
        }

    var columns: Int = 5
        set(value) {
            field = value
            initialize(listOf())
        }

    private val colorItemAdapter: ColorItemAdapter
        get() = ColorItemAdapter(context, object : BaseAdapter.OnItemClickListener<ColorPickerItem> {
            override fun onItemClick(content: ColorPickerItem, position: Int, v: View) {
                highlightItem(content)
            }
        })

    private fun highlightItem(content: ColorPickerItem) {
        colors = colors.map { c ->
            val isItemSelected = c.colorRes == content.colorRes
            c.copy(isSelected = isItemSelected)
        }
    }

    val selectedItem: ColorPickerItem?
        get() = colors.find { it.isSelected }

    fun initialize(colorItems: List<ColorPickerItem>) {
        this.colors = colorItems

        vb.rvColorPicker.apply {
            layoutManager = GridLayoutManager(context, columns)
            adapter = colorItemAdapter
        }

    }

    private fun updateRecyclerViewAdapter(values: List<ColorPickerItem>) {
        colorItemAdapter.updateData(values)
    }
}