package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent

class LoginLoadingFragment : BaseFragment() {

    override val layoutId: Int = R.layout.login_loading_fragment

    override fun setupViews() {
    }

    override fun injectToGraph(appComponent: AppComponent) {
    }

    override fun bindViewModel() {
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance() = LoginLoadingFragment()
    }
}