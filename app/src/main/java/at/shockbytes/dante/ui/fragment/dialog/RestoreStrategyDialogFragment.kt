package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
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
            val v = LayoutInflater.from(context).inflate(R.layout.dialogfragment_restore_strategy, null, false)
            v.findViewById<Button>(R.id.dialogfragment_restore_strategy_btn_merge).setOnClickListener {
                strategyListener?.invoke(BackupManager.RestoreStrategy.MERGE)
                dismiss()
            }
            v.findViewById<Button>(R.id.dialogfragment_restore_strategy_btn_overwrite).setOnClickListener {
                strategyListener?.invoke(BackupManager.RestoreStrategy.OVERWRITE)
                dismiss()
            }
            return v
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setTitle(R.string.backup_restore_strategy_title)
                .setIcon(R.drawable.ic_google_drive)
                .setView(strategyView)
                .create()
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
