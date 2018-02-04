package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog

/**
 * @author Martin Macheiner
 * Date: 02.01.2018.
 */

class SimpleRequestDialogFragment : DialogFragment() {

    private lateinit var title: String
    private lateinit var message: String
    private var icon: Int = 0
    private var positiveTextId: Int = 0

    private var acceptListener: (() -> Unit)? = null

    private var declineListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments.getString(argTitle)
        message = arguments.getString(argMessage)
        icon = arguments.getInt(argIcon)
        positiveTextId = arguments.getInt(argPositiveText)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(icon)
                .setMessage(message)
                .setPositiveButton(positiveTextId) { _, _ -> acceptListener?.invoke() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> declineListener?.invoke() }
                .create()
    }

    fun setOnAcceptListener(listener: () -> Unit): SimpleRequestDialogFragment {
        acceptListener = listener
        return this
    }

    fun setOnDeclineListener(listener: () -> Unit): SimpleRequestDialogFragment {
        declineListener = listener
        return this
    }

    companion object {

        private const val argTitle = "title"
        private const val argMessage = "message"
        private const val argIcon = "icon"
        private const val argPositiveText = "positive_text"

        fun newInstance(title: String, message: String, icon: Int,
                        positiveText: Int = android.R.string.yes): SimpleRequestDialogFragment {
            val fragment = SimpleRequestDialogFragment()
            val args = Bundle(4)
            args.putString(argTitle, title)
            args.putString(argMessage, message)
            args.putInt(argIcon, icon)
            args.putInt(argPositiveText, positiveText)
            fragment.arguments = args
            return fragment
        }
    }



}