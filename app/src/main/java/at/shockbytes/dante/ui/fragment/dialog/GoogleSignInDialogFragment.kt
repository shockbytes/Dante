package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.util.tracking.Tracker
import com.google.android.gms.common.SignInButton
import com.jakewharton.rxbinding2.view.RxView
import kotterknife.bindView
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 30.12.2017.
 */

class GoogleSignInDialogFragment : BaseDialogFragment() {

    @Inject
    protected lateinit var tracker: Tracker

    private val signInButton: SignInButton by bindView(R.id.dialogfragment_login_sign_in_button)

    private val laterButton: Button by bindView(R.id.dialogfragment_login_btn_later)

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

        RxView.clicks(signInButton).subscribe {
            tracker.trackGoogleLogin(true)
            signInListener?.invoke()
            dismiss()
        }
        RxView.clicks(laterButton).subscribe {
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