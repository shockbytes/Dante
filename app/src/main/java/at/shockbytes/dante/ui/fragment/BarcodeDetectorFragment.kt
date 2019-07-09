package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent

class BarcodeDetectorFragment: BaseFragment() {

    override val layoutId: Int = R.layout.fragment_barcode_detector

    override fun setupViews() {
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance(): BarcodeDetectorFragment {
            return BarcodeDetectorFragment()
        }
    }
}