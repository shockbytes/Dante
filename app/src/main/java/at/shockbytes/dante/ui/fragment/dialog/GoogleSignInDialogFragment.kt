package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.util.tracking.Tracker
import com.jakewharton.rxbinding2.view.RxView
import kotlinx.android.synthetic.main.dialogfragment_login.*
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 30.12.2017.
 */

class GoogleSignInDialogFragment : BaseDialogFragment() {

    @Inject
    protected lateinit var tracker: Tracker

    private val loginView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_login, null, false)

    private var signInListener: (() -> Unit)? = null

    private var maybeLaterListener: (() -> Unit)? = null

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setTitle(getString(R.string.dialog_login_title))
                .setIcon(R.drawable.ic_google)
                .setView(loginView)
                .create()
    }

    override fun onStart() {
        super.onStart()
        setupViews()
    }

    fun setSignInListener(listener: () -> Unit): GoogleSignInDialogFragment {
        signInListener = listener
        return this
    }

    fun setMaybeLaterListener(listener: () -> Unit): GoogleSignInDialogFragment {
        maybeLaterListener = listener
        return this
    }

    private fun setupViews() {

        RxView.clicks(btnDialogFragmentLoginSignIn).subscribe {
            tracker.trackGoogleLogin(true)
            signInListener?.invoke()
            dismiss()
        }
        RxView.clicks(btnDialogFragmentLoginLater).subscribe {
            tracker.trackGoogleLogin(false)
            maybeLaterListener?.invoke()
            dismiss()
        }
    }

    companion object {

        fun newInstance(): GoogleSignInDialogFragment {
            val fragment = GoogleSignInDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

    }

}