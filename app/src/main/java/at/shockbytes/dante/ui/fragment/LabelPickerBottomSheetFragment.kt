package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.injection.AppComponent

class LabelPickerBottomSheetFragment : BaseBottomSheetFragment() {

    override val layoutRes: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun injectToGraph(appComponent: AppComponent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bindViewModel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unbindViewModel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setupViews() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    companion object {

        fun newInstance(): LabelPickerBottomSheetFragment {
            return LabelPickerBottomSheetFragment()
        }
    }
}