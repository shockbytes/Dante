package at.shockbytes.dante.ui.custom.colorpicker

import android.R.animator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import at.shockbytes.dante.R
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.synthetic.main.color_picker_view.view.*

class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    var colors: List<ColorPickerItem> = listOf()
        set(value) {
            field = value
            updateRecyclerViewAdapter(value)
        }

    var columns: Int = 4
        set(value) {
            field = value
            initializeRecyclerView()
        }

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

    var onItemSelectedListener: ((ColorPickerItem) -> Unit)? = null

    init {
        View.inflate(context, R.layout.color_picker_view, this)
        initializeRecyclerView()
    }

    private fun initializeRecyclerView() {
        rv_color_picker.apply {
            layoutManager = GridLayoutManager(context, columns)
            adapter = colorItemAdapter
        }
    }

    private fun updateRecyclerViewAdapter(values: List<ColorPickerItem>) {
        colorItemAdapter.updateData(values)
    }
}