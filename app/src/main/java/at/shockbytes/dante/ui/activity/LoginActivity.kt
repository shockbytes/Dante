package at.shockbytes.dante.ui.activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.fragment.LoginFragment
import at.shockbytes.dante.ui.fragment.OnboardingFragment
import at.shockbytes.dante.ui.viewmodel.LoginViewModel
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[LoginViewModel::class.java]

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
                    showOnboardingFragment()
                }
                is LoginViewModel.LoginState.LoggedIn -> {
                    ActivityNavigator.navigateTo(this, ActivityNavigator.Destination.Main())
                }
                is LoginViewModel.LoginState.LoggedOut -> {
                    showLoginFragment()
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
                .replace(android.R.id.content, OnboardingFragment.newInstance())
                .commit()
    }
}