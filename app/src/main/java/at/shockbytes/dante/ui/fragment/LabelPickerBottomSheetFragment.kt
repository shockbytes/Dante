package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent

class LabelPickerBottomSheetFragment : BaseBottomSheetFragment() {

    override val layoutRes: Int = R.layout.fragment_label_picker_bottom_sheet

    override fun injectToGraph(appComponent: AppComponent) {
    }

    override fun bindViewModel() {
    }

    override fun unbindViewModel() {
    }

    override fun setupViews() {
    }

    companion object {

        fun newInstance(): LabelPickerBottomSheetFragment {
            return LabelPickerBottomSheetFragment()
        }
    }
}