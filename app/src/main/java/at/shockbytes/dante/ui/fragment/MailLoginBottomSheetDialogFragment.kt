package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent

class MailLoginBottomSheetDialogFragment : BaseBottomSheetFragment() {

    override val layoutRes: Int = R.layout.mail_login_bottom_sheet

    private var onCredentialsEnteredListener: ((mail: String, password: String) -> Unit)? = null

    override fun injectToGraph(appComponent: AppComponent) = Unit
    override fun bindViewModel() = Unit
    override fun unbindViewModel() = Unit

    override fun setupViews() {
        // TODO
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