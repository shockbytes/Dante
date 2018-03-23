package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import at.shockbytes.dante.R

/**
 * @author Martin Macheiner
 * Date: 16.01.2018.
 *
 * DialogFragment without usual frame and DialogFragment specific buttons
 *
 */

abstract class InteractiveViewDialogFragment<T>: BaseDialogFragment() {

    protected var applyListener: ((T) -> Unit)? = null

    abstract val containerView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setView(containerView)
                .create()
    }

    fun setOnApplyListener(listener: (T) -> Unit): InteractiveViewDialogFragment<T> {
        applyListener = listener
        return this
    }


    override fun onResume() {
        super.onResume()
        setupViews()
    }

    abstract fun setupViews()

}