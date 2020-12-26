package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.core.sdkVersionOrAbove
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.fragment.LoginFragment
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
                }
                is LoginViewModel.LoginState.LoggedOut -> {
                    showLoginFragment()
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
                .replace(android.R.id.content, LoginFragment.newInstance())
                .commit()
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}