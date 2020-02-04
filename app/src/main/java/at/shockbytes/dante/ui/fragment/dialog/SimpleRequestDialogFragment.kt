package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import at.shockbytes.dante.R
import kotterknife.bindView

/**
 * Author:  Martin Macheiner
 * Date:    02.01.2018
 */
class SimpleRequestDialogFragment : DialogFragment() {

    private val txtHeader: TextView by bindView(R.id.txtDialogFragmentSimpleRequestHeader)
    private val txtMessage: TextView by bindView(R.id.txtDialogFragmentSimpleRequestMessage)

    private lateinit var title: String
    private lateinit var message: String
    private var icon: Int = 0
    private var positiveTextId: Int = 0

    private var acceptListener: (() -> Unit)? = null

    private var declineListener: (() -> Unit)? = null

    private val dialogView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_simple_request, null, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString(argTitle) ?: ""
        message = arguments?.getString(argMessage) ?: ""
        icon = arguments?.getInt(argIcon) ?: R.mipmap.ic_launcher
        positiveTextId = arguments?.getInt(argPositiveText) ?: android.R.string.ok
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setPositiveButton(positiveTextId) { _, _ -> acceptListener?.invoke() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> declineListener?.invoke() }
                .create()
    }

    override fun onResume() {
        super.onResume()

        txtHeader.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
        txtHeader.text = title
        txtMessage.text = message
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

        fun newInstance(
            title: String,
            message: String,
            icon: Int,
            positiveText: Int = android.R.string.yes
        ): SimpleRequestDialogFragment {
            return SimpleRequestDialogFragment().apply {
                arguments = Bundle(4).apply {
                    putString(argTitle, title)
                    putString(argMessage, message)
                    putInt(argIcon, icon)
                    putInt(argPositiveText, positiveText)
                }
            }
        }
    }
}