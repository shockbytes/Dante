package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import at.shockbytes.dante.R
import at.shockbytes.dante.core.sdkVersionOrAbove
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.injection.ViewModelFactory
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.ui.activity.core.ActivityTransition
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.fragment.LoginFragment
import at.shockbytes.dante.ui.fragment.LoginLoadingFragment
import at.shockbytes.dante.ui.viewmodel.LoginViewModel
import at.shockbytes.dante.util.runDelayed
import at.shockbytes.dante.util.viewModelOf
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var vmFactory: ViewModelFactory

    override val activityTransition = ActivityTransition.slideFromBottom()

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
        setFullscreen()
        hideSystemBars()
        bindViewModel()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    private fun bindViewModel() {
        viewModel.resolveLoginState()
        viewModel.getLoginState().observe(this, Observer(::handleLoginState))
    }

    private fun handleLoginState(state: LoginViewModel.LoginState) {
        when (state) {
            is LoginViewModel.LoginState.LoggedIn -> {
                runDelayed(300) {
                    ActivityNavigator.navigateTo(this, Destination.Main())
                }
            }
            is LoginViewModel.LoginState.LoggedOut -> {
                showLoginFragment()
            }
            is LoginViewModel.LoginState.Error -> {
                showSnackbar(getString(state.errorMessageRes))
            }
            LoginViewModel.LoginState.Loading -> {
                showLoadingFragment()
            }
        }
    }

    private fun setFullscreen() {
        if (sdkVersionOrAbove(Build.VERSION_CODES.R)) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }

    private fun hideSystemBars() {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun showLoginFragment() {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(android.R.id.content, LoginFragment.newInstance())
            .commit()
    }

    private fun showLoadingFragment() {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(android.R.id.content, LoginLoadingFragment.newInstance())
            .commit()
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}