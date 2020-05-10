package at.shockbytes.dante.ui.activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.fragment.LoginFragment
import at.shockbytes.dante.ui.fragment.OnboardingFragment
import at.shockbytes.dante.ui.viewmodel.LoginViewModel
import at.shockbytes.dante.util.viewModelOf
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)

        bindViewModel()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    private fun bindViewModel() {
        viewModel.requestLoginState()

        viewModel.getLoginState().observe(this, Observer { state ->
            when (state) {
                is LoginViewModel.LoginState.FirstAppOpen -> {
                    // TODO Maybe offer instant sign-on solution later
                    ActivityNavigator.navigateTo(this, Destination.Main())
                }
                is LoginViewModel.LoginState.LoggedIn -> {
                    ActivityNavigator.navigateTo(this, Destination.Main())
                }
                is LoginViewModel.LoginState.LoggedOut -> {
                    showLoginFragment()
                }
                is LoginViewModel.LoginState.ShowOnboarding -> {
                    showOnboardingFragment()
                }
            }
        })
    }

    private fun showLoginFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, LoginFragment.newInstance())
                .commit()
    }

    private fun showOnboardingFragment() {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(android.R.id.content, OnboardingFragment.newInstance())
            .commit()
    }
}