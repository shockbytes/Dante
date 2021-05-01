package at.shockbytes.dante.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import at.shockbytes.dante.R
import at.shockbytes.dante.core.login.MailLoginCredentials
import at.shockbytes.dante.databinding.MailLoginBottomSheetBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.injection.ViewModelFactory
import at.shockbytes.dante.ui.viewmodel.MailLoginViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class MailLoginBottomSheetDialogFragment : BaseBottomSheetFragment<MailLoginBottomSheetBinding>() {

    @Inject
    protected lateinit var vmFactory: ViewModelFactory

    private val viewModel: MailLoginViewModel by lazy { viewModelOf(vmFactory) }

    private var mailLoginState: MailLoginViewModel.MailLoginState by argument()

    private var onCredentialsEnteredListener: ((credentials: MailLoginCredentials) -> Unit)? = null

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): MailLoginBottomSheetBinding {
        return MailLoginBottomSheetBinding.inflate(inflater, root, attachToRoot)
    }

    override fun injectToGraph(appComponent: AppComponent) = appComponent.inject(this)

    override fun bindViewModel() {
        viewModel.initialize(mailLoginState)

        viewModel.getStep().observe(this, Observer(::handleStep))

        viewModel.getMailResetAction()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleMailResetAction)
            .addTo(compositeDisposable)

        viewModel.isMailValid()
            .doOnNext(vb.btnLoginMailContinue::setEnabled)
            .subscribe(::handleMailValidation)
            .addTo(compositeDisposable)

        viewModel.isPasswordValid()
            .subscribe(::handlePasswordValidation)
            .addTo(compositeDisposable)

        viewModel.onGoogleMailLoginAttempt()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                vb.editTextMailAddress.setText("")
                showGoogleMailLogin()
            }
            .addTo(compositeDisposable)

        Observable
            .combineLatest(
                viewModel.isPasswordValid(),
                viewModel.isMailValid(),
                { isPasswordValid, isMailValid -> isPasswordValid && isMailValid }
            )
            .subscribe(vb.btnLoginMail::setEnabled)
            .addTo(compositeDisposable)
    }

    private fun showGoogleMailLogin() {
        MaterialDialog(requireContext()).show {
            icon(R.drawable.ic_google)
            title(text = getString(R.string.login_mail_with_google_account))
            message(text = getString(R.string.login_mail_with_google_account_message))
            positiveButton(R.string.got_it) {
                dismiss()
            }
            cancelOnTouchOutside(true)
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    private fun handleStep(step: MailLoginViewModel.MailLoginStep) {
        when (step) {
            is MailLoginViewModel.MailLoginStep.MailVerification -> {
                setMailVerificationStep()
            }
            is MailLoginViewModel.MailLoginStep.PasswordVerification -> {
                setPasswordVerificationStep(step)
            }
        }
    }

    private fun setMailVerificationStep() {

        vb.tvLoginMailHeader.setText(R.string.login_mail_enter_mail_address)

        vb.tilTextMailAddress.translationY = 60f
        vb.editTextMailAddress.apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
            isEnabled = true
        }

        vb.tilTextMailPassword.setVisible(false, View.INVISIBLE)
        vb.btnLoginMailContinue.setVisible(true)
        vb.btnLoginMail.setVisible(false, View.INVISIBLE)
    }

    private fun setPasswordVerificationStep(
        step: MailLoginViewModel.MailLoginStep.PasswordVerification
    ) {

        vb.tvLoginMailHeader.setText(step.textHeader)

        vb.tilTextMailAddress.animate().translationY(0f).start()
        vb.editTextMailAddress.apply {
            imeOptions = EditorInfo.IME_ACTION_NEXT
            isEnabled = step.isEmailEnabled
        }

        if (step.focusOnPasswordField) {
            vb.editTextMailPassword.requestFocus()
        }

        vb.tilTextMailPassword.apply {
            alpha = 0f
            scaleX = 0.7f
            scaleY = 0.7f
            visibility = View.VISIBLE

            animate()
                .setStartDelay(100L)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .start()
        }

        vb.btnLoginMailContinue.animate()
            .setStartDelay(100L)
            .alpha(0f)
            .scaleX(0.7f)
            .scaleY(0.7f)
            .withEndAction {
                vb.btnLoginMailContinue.setVisible(false, View.INVISIBLE)
            }
            .start()

        val loginText = if (step.isSignUp) R.string.sign_up_with_mail else R.string.login_with_mail

        vb.btnLoginMail.apply {
            setText(loginText)
            alpha = 0f
            scaleX = 0.7f
            scaleY = 0.7f
            visibility = View.VISIBLE

            animate()
                .setStartDelay(400L)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .start()
        }

        if (step.isSignUp) {
            vb.btnLoginMailForgotPassword.setVisible(false)
        } else {
            vb.btnLoginMailForgotPassword.apply {
                alpha = 0f
                scaleX = 0.7f
                scaleY = 0.7f
                visibility = View.VISIBLE
                isEnabled = true

                animate()
                    .setStartDelay(700L)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .start()
            }
        }
    }

    private fun handleMailResetAction(action: MailLoginViewModel.MailResetAction) {
        when (action) {
            is MailLoginViewModel.MailResetAction.Success -> {
                showToast(getString(R.string.reset_password_success, action.mailAddress))
            }
            is MailLoginViewModel.MailResetAction.Error -> {
                showToast(getString(R.string.reset_password_error, action.mailAddress))
                dismiss()
            }
        }
    }

    private fun handleMailValidation(isMailValid: Boolean) {
        vb.tilTextMailAddress.apply {
            isErrorEnabled = !isMailValid
            error = if (isMailValid) null else getString(R.string.invalid_email_format)
        }
    }

    private fun handlePasswordValidation(isPasswordValid: Boolean) {
        vb.tilTextMailPassword.apply {
            isErrorEnabled = !isPasswordValid
            error = if (isPasswordValid) null else getString(R.string.invalid_password_format)
        }
    }

    override fun unbindViewModel() = Unit

    override fun setupViews() {

        RxTextView.textChanges(vb.editTextMailAddress)
            .skipInitialValue()
            .subscribe(viewModel::verifyMailAddress)
            .addTo(compositeDisposable)

        RxTextView.textChanges(vb.editTextMailPassword)
            .subscribe(viewModel::verifyPassword)
            .addTo(compositeDisposable)

        vb.btnLoginMailContinue.setOnClickListener {
            viewModel.checkIfAccountExistsForMailAddress()
        }

        vb.btnLoginMail.setOnClickListener {
            onCredentialsEnteredListener?.invoke(viewModel.getMailLoginCredentials())
            dismiss()
        }

        vb.btnLoginMailForgotPassword.setOnClickListener {
            // Disable button to prevent multiple click events
            it.isEnabled = false
            viewModel.userForgotPassword()
        }
    }

    fun setOnCredentialsEnteredListener(
        listener: ((credentials: MailLoginCredentials) -> Unit)
    ): MailLoginBottomSheetDialogFragment {
        return apply {
            this.onCredentialsEnteredListener = listener
        }
    }

    companion object {

        fun newInstance(
            mailLoginState: MailLoginViewModel.MailLoginState
        ): MailLoginBottomSheetDialogFragment {
            return MailLoginBottomSheetDialogFragment().apply {
                this.mailLoginState = mailLoginState
            }
        }
    }
}