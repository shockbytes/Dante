package at.shockbytes.dante.ui.fragment.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.databinding.DialogfragmentCreateLabelBinding
import at.shockbytes.dante.ui.custom.colorpicker.ColorPickerItems
import at.shockbytes.dante.util.DanteUtils.dpToPixelF
import at.shockbytes.dante.util.HexColor
import at.shockbytes.dante.util.hideKeyboard
import at.shockbytes.dante.util.layoutInflater
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

@SuppressLint("ResourceType")
class CreateLabelDialogFragmentWrapper(
    private val alreadyCreatedLabels: List<BookLabel>
) {

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

    private var applyListener: ((label: BookLabel) -> Unit)? = null

    fun setOnApplyListener(listener: (label: BookLabel) -> Unit): CreateLabelDialogFragmentWrapper {
        return apply {
            this.applyListener = listener
        }
    }

    fun show(fragment: Fragment) {

        val context = fragment.requireContext()

        val vb = DialogfragmentCreateLabelBinding.inflate(context.layoutInflater())

        val dialog = MaterialDialog(context)
            .customView(view = vb.root)
            .cornerRadius(context.dpToPixelF(6))
            .cancelOnTouchOutside(true)

        vb.colorPickerLabel.initialize(ColorPickerItems.fromColorResources(colors, preSelectedIndex = 0))
        vb.btnNewLabel.setOnClickListener {

            val title = vb.etNameLabel.text.toString()
            val labelColor = vb.colorPickerLabel.selectedItem?.let { colorPickerItem ->
                context.getString(colorPickerItem.colorRes)
            }

            when {
                doesTitleAlreadyExist(title) -> {
                    Toast.makeText(context, R.string.new_label_title_exists, Toast.LENGTH_LONG).show()
                }
                (canCreateNewLabel(title, labelColor)) -> {
                    val label = BookLabel.unassignedLabel(title, HexColor.ofString(labelColor!!))
                    applyListener?.invoke(label)
                    fragment.hideKeyboard()
                    dialog.dismiss()
                }
                else -> {
                    Toast.makeText(context, R.string.new_label_error, Toast.LENGTH_LONG).show()
                }
            }
        }

        dialog.show()
    }

    private fun doesTitleAlreadyExist(title: String): Boolean {
        return alreadyCreatedLabels.any { it.title == title }
    }

    private fun canCreateNewLabel(title: String, labelColor: String?): Boolean {
        return title.isNotEmpty() && !labelColor.isNullOrEmpty() && title.length <= MAX_TITLE_LENGTH
    }

    companion object {

        private const val MAX_TITLE_LENGTH = 16

        fun newInstance(createdLabels: List<BookLabel>): CreateLabelDialogFragmentWrapper {
            return CreateLabelDialogFragmentWrapper(createdLabels)
        }
    }

}