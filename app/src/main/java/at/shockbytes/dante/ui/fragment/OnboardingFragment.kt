package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.LoginActivity
import at.shockbytes.dante.ui.adapter.OnboardingAdapter
import at.shockbytes.dante.ui.viewmodel.LoginViewModel
import at.shockbytes.dante.util.viewModelOfActivity
import kotlinx.android.synthetic.main.fragment_onboarding.*
import javax.inject.Inject

class OnboardingFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_onboarding

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOfActivity(activity as LoginActivity, vmFactory)
    }

    override fun setupViews() {
        context?.let { ctx ->
            fragment_onboarding_vp.setCreativeViewPagerAdapter(OnboardingAdapter(ctx))
        }
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