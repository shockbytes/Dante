package at.shockbytes.dante.ui.fragment

import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import at.shockbytes.dante.R
import at.shockbytes.dante.core.login.MailLoginCredentials
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.injection.ViewModelFactory
import at.shockbytes.dante.ui.viewmodel.MailLoginViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_login.btn_login_mail
import kotlinx.android.synthetic.main.mail_login_bottom_sheet.*
import javax.inject.Inject

class MailLoginBottomSheetDialogFragment : BaseBottomSheetFragment() {

    @Inject
    protected lateinit var vmFactory: ViewModelFactory

    private val viewModel: MailLoginViewModel by lazy { viewModelOf(vmFactory) }

    private var mailLoginState: MailLoginViewModel.MailLoginState by argument()

    private var onCredentialsEnteredListener: ((credentials: MailLoginCredentials) -> Unit)? = null

    override val layoutRes: Int = R.layout.mail_login_bottom_sheet

    override fun injectToGraph(appComponent: AppComponent) = appComponent.inject(this)

    override fun bindViewModel() {
        viewModel.initialize(mailLoginState)

        viewModel.getStep().observe(this, Observer(::handleStep))

        viewModel.isMailValid()
            .doOnNext(btn_login_mail_continue::setEnabled)
            .subscribe(::handleMailValidation)
            .addTo(compositeDisposable)

        viewModel.isPasswordValid()
            .subscribe(::handlePasswordValidation)
            .addTo(compositeDisposable)

        Observable
            .combineLatest(
                viewModel.isPasswordValid(),
                viewModel.isMailValid(),
                { isPasswordValid, isMailValid -> isPasswordValid && isMailValid }
            )
            .subscribe(btn_login_mail::setEnabled)
            .addTo(compositeDisposable)
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

        tvLoginMailHeader.setText(R.string.login_mail_enter_mail_address)

        tilTextMailAddress.translationY = 175f
        editTextMailAddress.apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
            isEnabled = true
        }

        tilTextMailPassword.setVisible(false, View.INVISIBLE)
        btn_login_mail_continue.setVisible(true)
        btn_login_mail.setVisible(false, View.INVISIBLE)
    }

    private fun setPasswordVerificationStep(
        step: MailLoginViewModel.MailLoginStep.PasswordVerification
    ) {

        tvLoginMailHeader.setText(step.textHeader)

        tilTextMailAddress.animate().translationY(0f).start()
        editTextMailAddress.apply {
            imeOptions = EditorInfo.IME_ACTION_NEXT
            isEnabled = step.isEmailEnabled
        }

        editTextMailPassword.requestFocus()

        tilTextMailPassword.apply {
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

        btn_login_mail_continue.animate()
            .setStartDelay(100L)
            .alpha(0f)
            .scaleX(0.7f)
            .scaleY(0.7f)
            .withEndAction {
                btn_login_mail_continue.setVisible(false, View.INVISIBLE)
            }
            .start()

        val loginText = if (step.isSignUp) R.string.sign_up_with_mail else R.string.login_with_mail

        btn_login_mail.apply {
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
    }

    private fun handleMailValidation(isMailValid: Boolean) {
        tilTextMailAddress.apply {
            isErrorEnabled = !isMailValid
            error = if (isMailValid) null else getString(R.string.invalid_email_format)
        }
    }

    private fun handlePasswordValidation(isPasswordValid: Boolean) {
        tilTextMailPassword.apply {
            isErrorEnabled = !isPasswordValid
            error = if (isPasswordValid) null else getString(R.string.invalid_password_format)
        }
    }

    override fun unbindViewModel() = Unit

    override fun setupViews() {

        RxTextView.textChanges(editTextMailAddress)
            .skipInitialValue()
            .subscribe(viewModel::verifyMailAddress)
            .addTo(compositeDisposable)

        RxTextView.textChanges(editTextMailPassword)
            .subscribe(viewModel::verifyPassword)
            .addTo(compositeDisposable)

        btn_login_mail_continue.setOnClickListener {
            viewModel.checkIfAccountExistsForMailAddress()
        }

        btn_login_mail.setOnClickListener {
            onCredentialsEnteredListener?.invoke(viewModel.getMailLoginCredentials())
            dismiss()
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