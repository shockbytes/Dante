package at.shockbytes.dante.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.LoginActivity
import at.shockbytes.dante.ui.viewmodel.LoginViewModel
import javax.inject.Inject

class OnboardingFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_onboarding

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity as LoginActivity, vmFactory)[LoginViewModel::class.java]
    }

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

        fun newInstance(): OnboardingFragment {
            return OnboardingFragment()
        }
    }
}