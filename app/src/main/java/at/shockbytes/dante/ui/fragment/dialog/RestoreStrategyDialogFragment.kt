package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import at.shockbytes.dante.R
import at.shockbytes.dante.util.RestoreStrategy

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2016
 */
class RestoreStrategyDialogFragment : DialogFragment() {

    private var strategyListener: ((RestoreStrategy) -> Unit)? = null

    private val strategyView: View
        get() {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_restore_strategy, null, false)
            setupViews(view)
            return view
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setView(strategyView)
            .create()
            .also { it.requestWindowFeature(Window.FEATURE_NO_TITLE) }
    }

    private fun setupViews(view: View) {
        view.findViewById<View>(R.id.dialogfragment_restore_strategy_btn_merge).setOnClickListener {
            strategyListener?.invoke(RestoreStrategy.MERGE)
            dismiss()
        }

        view.findViewById<View>(R.id.dialogfragment_restore_strategy_btn_overwrite).setOnClickListener {
            strategyListener?.invoke(RestoreStrategy.OVERWRITE)
            dismiss()
        }
    }

    fun setOnRestoreStrategySelectedListener(
        listener: (RestoreStrategy) -> Unit
    ): RestoreStrategyDialogFragment {
        this.strategyListener = listener
        return this
    }

    companion object {

        fun newInstance(): RestoreStrategyDialogFragment {
            return RestoreStrategyDialogFragment()
        }
    }
}
