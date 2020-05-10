package at.shockbytes.dante.ui.fragment.onboarding

import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.fragment.BaseFragment

class OnboardingLoginFragment : BaseFragment() {

    override val layoutId: Int = at.shockbytes.dante.R.layout.fragment_onboarding_login

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

        fun newInstance() = OnboardingLoginFragment()
    }
}