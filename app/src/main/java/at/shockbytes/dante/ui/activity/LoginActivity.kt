package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.core.sdkVersionOrAbove
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.ActivityTransition
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.fragment.LoginFragment
import at.shockbytes.dante.ui.viewmodel.LoginViewModel
import at.shockbytes.dante.util.viewModelOf
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    override val activityTransition = ActivityTransition.slideFromBottom()

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
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
        viewModel.getLoginState().observe(this, { state ->
            when (state) {
                is LoginViewModel.LoginState.LoggedIn -> {
                    // ActivityNavigator.navigateTo(this, Destination.Main())
                    supportFinishAfterTransition()
                }
                is LoginViewModel.LoginState.LoggedOut -> {
                    showLoginFragment()
                }
                is LoginViewModel.LoginState.Error -> {
                    showSnackbar(getString(state.errorMessageRes))
                }
            }
        })
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

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}