package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.showKeyboard
import com.google.android.material.textfield.TextInputLayout
import kotterknife.bindView

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2016
 */
class SimpleInputDialogFragment : DialogFragment() {

    private val inputField: EditText by bindView(R.id.et_simple_input)

    private var icon: Int by argument()
    private var title: Int by argument()
    private var message: Int by argument()
    private var hint: Int by argument()
    private var positiveButtonText: Int by argument()

    private var inputListener: ((String) -> Unit)? = null

    private val queryView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_simple_input, null, false)
            .also(::populateViews)

    private fun populateViews(view: View) {
        with(view) {
            findViewById<TextView>(R.id.tv_simple_input_header).apply {
                setText(title)
                val iconDrawable = ContextCompat.getDrawable(requireContext(), icon)
                setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null)
            }
            findViewById<TextView>(R.id.tv_simple_input_message).setText(message)
            findViewById<TextInputLayout>(R.id.til_simple_input).setHint(hint)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setView(queryView)
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setPositiveButton(positiveButtonText) { _, _ ->
                inputListener?.invoke(inputField.text.toString())
            }
            .create()
            .also { it.requestWindowFeature(Window.FEATURE_NO_TITLE) }
            .also { it.window?.setSoftInputMode(SOFT_INPUT_STATE_VISIBLE) }
    }

    fun setOnInputEnteredListener(listener: (String) -> Unit): SimpleInputDialogFragment {
        return apply {
            this.inputListener = listener
        }
    }

    override fun onResume() {
        super.onResume()
        showKeyboard(inputField)
    }

    companion object {

        fun newInstance(
            icon: Int,
            title: Int,
            message: Int,
            hint: Int,
            positiveButtonText: Int
        ): SimpleInputDialogFragment{
            return SimpleInputDialogFragment().apply {
                this.icon = icon
                this.title = title
                this.message = message
                this.hint = hint
                this.positiveButtonText = positiveButtonText
            }
        }
    }
}
