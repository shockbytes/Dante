package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.btn_login_mail
import kotlinx.android.synthetic.main.mail_login_bottom_sheet.*

class MailLoginBottomSheetDialogFragment : BaseBottomSheetFragment() {

    override val layoutRes: Int = R.layout.mail_login_bottom_sheet

    private var onCredentialsEnteredListener: ((mail: String, password: String) -> Unit)? = null

    override fun injectToGraph(appComponent: AppComponent) = Unit
    override fun bindViewModel() = Unit
    override fun unbindViewModel() = Unit

    override fun setupViews() {
        // TODO Verify mail address

        // TODO Minimum password length of 6 characters

        btn_login_mail.setOnClickListener {
            val mail = editTextMailAddress.text?.toString() ?: ""
            val password = editTextMailPassword.text?.toString() ?: ""
            onCredentialsEnteredListener?.invoke(mail, password)

            dismiss()
        }
    }

    fun setOnCredentialsEnteredListener(
        listener: ((mail: String, password: String) -> Unit)
    ): MailLoginBottomSheetDialogFragment {
        return apply {
            this.onCredentialsEnteredListener = listener
        }
    }

    companion object {

        fun newInstance() = MailLoginBottomSheetDialogFragment()
    }
}