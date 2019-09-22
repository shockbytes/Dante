package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.util.addTo
import com.google.android.gms.common.SignInButton
import com.jakewharton.rxbinding2.view.RxView
import kotterknife.bindView

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017
 */
class GoogleSignInDialogFragment : BaseDialogFragment() {

    private val signInButton: SignInButton by bindView(R.id.dialogfragment_login_sign_in_button)

    private val laterButton: Button by bindView(R.id.dialogfragment_login_btn_later)

    private val loginView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_login, null, false)

    private var signInListener: ((Boolean) -> Unit)? = null

    private var maybeLaterListener: (() -> Unit)? = null

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setView(loginView)
                .create()
                .also { it.requestWindowFeature(Window.FEATURE_NO_TITLE) }
    }

    override fun onStart() {
        super.onStart()
        setupViews()
    }

    fun setSignInListener(listener: (Boolean) -> Unit): GoogleSignInDialogFragment {
        signInListener = listener
        return this
    }

    fun setMaybeLaterListener(listener: () -> Unit): GoogleSignInDialogFragment {
        maybeLaterListener = listener
        return this
    }

    private fun setupViews() {

        RxView.clicks(signInButton).subscribe {
            signInListener?.invoke(false)
            dismiss()
        }.addTo(compositeDisposable)

        // TODO Add a online sign in check box!!!

        RxView.clicks(laterButton).subscribe {
            maybeLaterListener?.invoke()
            dismiss()
        }.addTo(compositeDisposable)
    }

    companion object {

        fun newInstance(): GoogleSignInDialogFragment {
            return GoogleSignInDialogFragment()
        }
    }
}