package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.DialogfragmentSimpleRequestBinding
import at.shockbytes.dante.util.arguments.argument

/**
 * Author:  Martin Macheiner
 * Date:    02.01.2018
 */
// TODO Deprecated
@Deprecated("Remove this view!")
class SimpleRequestDialogFragment : DialogFragment() {

    private var title: String by argument()
    private var message: String by argument()
    private var icon: Int by argument()
    private var positiveTextId: Int by argument()

    private var acceptListener: (() -> Unit)? = null

    private var declineListener: (() -> Unit)? = null

    private val dialogView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_simple_request, null, false)

    private val vb: DialogfragmentSimpleRequestBinding
        get() = DialogfragmentSimpleRequestBinding.bind(dialogView)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(positiveTextId) { _, _ -> acceptListener?.invoke() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> declineListener?.invoke() }
                .create()
    }

    override fun onResume() {
        super.onResume()

        vb.txtDialogFragmentSimpleRequestHeader.apply {
            setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
            text = title
        }
        vb.txtDialogFragmentSimpleRequestMessage.text = message
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

        fun newInstance(
            title: String,
            message: String,
            icon: Int,
            positiveText: Int = android.R.string.ok
        ): SimpleRequestDialogFragment {
            return SimpleRequestDialogFragment().apply {
                this.title = title
                this.message = message
                this.icon = icon
                this.positiveTextId = positiveText
            }
        }
    }
}