package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent

class OnlineStorageFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_online_storage

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

        fun newInstance(): OnlineStorageFragment {
            return OnlineStorageFragment()
        }
    }
}