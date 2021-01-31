package at.shockbytes.dante.ui.fragment.dialog

import android.annotation.SuppressLint
import android.os.Parcelable
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
import at.shockbytes.dante.util.HexColor
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.hideKeyboard
import kotlinx.android.parcel.Parcelize
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
        R.color.inspirations,
        R.color.teal_500,
        R.color.pink_500
    )

    private var alreadyCreatedLabels: CreatedLabels by argument()

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

            when {
                doesTitleAlreadyExist(title) -> {
                    Toast.makeText(context, R.string.new_label_title_exists, Toast.LENGTH_LONG).show()
                }
                (canCreateNewLabel(title, labelColor)) -> {
                    val label = BookLabel.unassignedLabel(title, HexColor.ofString(labelColor!!))
                    applyListener?.invoke(label)
                    activity?.hideKeyboard()
                    dismiss()
                }
                else -> {
                    Toast.makeText(context, R.string.new_label_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun doesTitleAlreadyExist(title: String): Boolean {
        return alreadyCreatedLabels.labels.any { it.title == title }
    }

    private fun canCreateNewLabel(title: String, labelColor: String?): Boolean {
        return title.isNotEmpty() && !labelColor.isNullOrEmpty() && title.length <= MAX_TITLE_LENGTH
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit

    companion object {

        private const val MAX_TITLE_LENGTH = 16

        fun newInstance(createdLabels: List<BookLabel>): CreateLabelDialogFragment {
            return CreateLabelDialogFragment().apply {
                alreadyCreatedLabels = CreatedLabels(createdLabels)
            }
        }

        @Parcelize
        private data class CreatedLabels(val labels: List<BookLabel>) : Parcelable
    }
}