package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.OnboardingAdapter
import kotlinx.android.synthetic.main.fragment_onboarding.*

class OnboardingFragment: BaseFragment() {

    override val layoutId: Int = at.shockbytes.dante.R.layout.fragment_onboarding

    override fun setupViews() {
        vp2_onboarding.apply {
            isUserInputEnabled = false
            adapter = OnboardingAdapter(requireActivity())
        }

        vp_dots.setupWithViewPager2(vp2_onboarding)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance(): OnboardingFragment {
            return OnboardingFragment()
        }
    }
}