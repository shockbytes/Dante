package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.BackupManager

/**
 * @author Martin Macheiner
 * Date: 30.08.2016.
 */
class RestoreStrategyDialogFragment : DialogFragment() {

    private var strategyListener: ((BackupManager.RestoreStrategy) -> Unit)? = null

    private val strategyView: View
        get() {
            val view = LayoutInflater.from(context)
                    .inflate(R.layout.dialogfragment_restore_strategy, null, false)
            setupViews(view)
            return view
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setTitle(R.string.backup_restore_strategy_title)
                .setIcon(R.drawable.ic_google_drive)
                .setView(strategyView)
                .create()
    }

    private fun setupViews(view: View) {
        view.findViewById<View>(R.id.dialogfragment_restore_strategy_btn_merge).setOnClickListener {
            strategyListener?.invoke(BackupManager.RestoreStrategy.MERGE)
            dismiss()
        }

        view.findViewById<View>(R.id.dialogfragment_restore_strategy_btn_overwrite).setOnClickListener {
            strategyListener?.invoke(BackupManager.RestoreStrategy.OVERWRITE)
            dismiss()
        }
    }

    fun setOnRestoreStrategySelectedListener(listener: (BackupManager.RestoreStrategy) -> Unit)
            : RestoreStrategyDialogFragment {
        this.strategyListener = listener
        return this
    }

    companion object {

        fun newInstance(): RestoreStrategyDialogFragment {
            return RestoreStrategyDialogFragment()
        }
    }
}
