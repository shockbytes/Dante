package at.shockbytes.dante.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.LoginActivity
import at.shockbytes.dante.ui.viewmodel.LoginViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.viewModelOfActivity
import com.github.florent37.inlineactivityresult.kotlin.startForResult
import kotlinx.android.synthetic.main.fragment_login.*
import javax.inject.Inject

class LoginFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_login

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOfActivity(activity as LoginActivity, vmFactory)
    }

    override fun setupViews() {

        btn_login_google.setOnClickListener {
            viewModel.requestGoogleLogin()
                .subscribe(::handleGoogleLoginRequest)
                .addTo(compositeDisposable)
        }

        btn_login_mail.setOnClickListener {
            MailLoginBottomSheetDialogFragment.newInstance()
                .setOnCredentialsEnteredListener(viewModel::loginWithMail)
                .show(parentFragmentManager, "mail-login-fragment")
        }

        btn_login_skip.setOnClickListener {
            viewModel.loginAnonymously()
        }
    }

    private fun handleGoogleLoginRequest(loginIntent: Intent) {
        startForResult(loginIntent) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                viewModel.loginWithGoogle(result.data!!)
            } else {
                showSnackbar(getString(R.string.login_error_google))
            }
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() = Unit

    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance() = LoginFragment()
    }
}