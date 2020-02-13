package at.shockbytes.dante.ui.fragment.dialog

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.custom.colorpicker.ColorPickerItems
import at.shockbytes.dante.ui.custom.colorpicker.ColorPickerView
import at.shockbytes.dante.util.hideKeyboard
import kotterknife.bindView

class CreateLabelDialogFragment : InteractiveViewDialogFragment<BookLabel>() {

    private val colors = listOf(
        R.color.tabcolor_upcoming,
        R.color.tabcolor_current,
        R.color.tabcolor_done,
        R.color.brown,
        R.color.color_error,
        R.color.nice_color,
        R.color.indigo,
        R.color.tabcolor_suggestions
    )

    private val colorPicker by bindView<ColorPickerView>(R.id.color_picker_label)
    private val btnCreate by bindView<Button>(R.id.btn_new_label)
    private val editTextTitle by bindView<EditText>(R.id.et_name_label)

    override val containerView: View
        get() = LayoutInflater.from(context).inflate(R.layout.dialogfragment_create_label, null, false)

    @SuppressLint("ResourceType")
    override fun setupViews() {
        colorPicker.colors = ColorPickerItems.fromColorResources(colors, preSelectedIndex = 0)

        btnCreate.setOnClickListener {

            val title = editTextTitle.text.toString()
            val labelColor = colorPicker.selectedItem?.let { colorPickerItem ->
                resources.getString(colorPickerItem.colorRes)
            }

            if (title.isNotEmpty() && !labelColor.isNullOrEmpty()) {
                val label = BookLabel(title, labelColor)
                applyListener?.invoke(label)
                activity?.hideKeyboard()
                dismiss()
            } else {
                Toast.makeText(context, R.string.new_label_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit

    companion object {

        fun newInstance(): CreateLabelDialogFragment {
            return CreateLabelDialogFragment()
        }
    }
}