package at.shockbytes.dante.ui.fragment.dialog

import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.custom.colorpicker.ColorPickerItems
import at.shockbytes.dante.ui.custom.colorpicker.ColorPickerView
import kotlinx.android.synthetic.main.dialogfragment_create_label.*
import kotterknife.bindView

class CreateLabelDialogFragment : InteractiveViewDialogFragment<BookLabel>() {

    private val colors = listOf(
        R.color.brown,
        R.color.tabcolor_upcoming,
        R.color.tabcolor_current,
        R.color.tabcolor_done,
        R.color.color_error,
        R.color.nice_color,
        R.color.indigo,
        R.color.tabcolor_suggestions
    )

    private val colorPicker: ColorPickerView by bindView(R.id.color_picker_label)

    override val containerView: View
        get() = LayoutInflater.from(context).inflate(R.layout.dialogfragment_create_label, null, false)

    override fun setupViews() {
        colorPicker.colors = ColorPickerItems.fromColorResources(colors, preSelectedIndex = 0)
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit

    companion object {

        fun newInstance(): CreateLabelDialogFragment {
            return CreateLabelDialogFragment()
        }
    }
}