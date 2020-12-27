package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.injection.ViewModelFactory
import at.shockbytes.dante.ui.viewmodel.MailLoginViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.viewModelOf
import com.jakewharton.rxbinding2.widget.RxTextView
import kotlinx.android.synthetic.main.fragment_login.btn_login_mail
import kotlinx.android.synthetic.main.mail_login_bottom_sheet.*
import javax.inject.Inject

class MailLoginBottomSheetDialogFragment : BaseBottomSheetFragment() {

    @Inject
    protected lateinit var vmFactory: ViewModelFactory

    private val viewModel: MailLoginViewModel by lazy { viewModelOf(vmFactory) }

    private var onCredentialsEnteredListener: ((mail: String, password: String, isSignUp: Boolean) -> Unit)? = null

    override val layoutRes: Int = R.layout.mail_login_bottom_sheet

    override fun injectToGraph(appComponent: AppComponent) = appComponent.inject(this)

    override fun bindViewModel() {

    }

    override fun unbindViewModel() = Unit

    override fun setupViews() {

        RxTextView.textChanges(editTextMailAddress)
            .subscribe(viewModel::verifyMailAddress)
            .addTo(compositeDisposable)

        RxTextView.textChanges(editTextMailPassword)
            .subscribe(viewModel::verifyPassword)
            .addTo(compositeDisposable)

        btn_login_mail.setOnClickListener {
            // TODO
            val mail = editTextMailAddress.text?.toString() ?: ""
            val password = editTextMailPassword.text?.toString() ?: ""
            onCredentialsEnteredListener?.invoke(mail, password, false)

            dismiss()
        }
    }

    fun setOnCredentialsEnteredListener(
        listener: ((mail: String, password: String, isSignUp: Boolean) -> Unit)
    ): MailLoginBottomSheetDialogFragment {
        return apply {
            this.onCredentialsEnteredListener = listener
        }
    }

    companion object {

        fun newInstance() = MailLoginBottomSheetDialogFragment()
    }
}