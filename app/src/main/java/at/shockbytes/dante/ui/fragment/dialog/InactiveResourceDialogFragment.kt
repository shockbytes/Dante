package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.dagger.AppComponent

class InactiveResourceDialogFragment : BaseDialogFragment() {

    override fun injectToGraph(appComponent: AppComponent) = Unit

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val backupProvider = arguments?.getSerializable(ARG_PROVIDER) as BackupStorageProvider

        return AlertDialog.Builder(requireContext())
            .setMessage(R.string.inactive_resource_message)
            .setTitle(backupProvider.title)
            .setIcon(backupProvider.icon)
            .setPositiveButton(R.string.got_it) { _, _ ->
                dismiss()
            }
            .create()
    }

    companion object {

        private const val ARG_PROVIDER = "arg_backup_provider"

        fun newInstance(provider: BackupStorageProvider): InactiveResourceDialogFragment {
            return InactiveResourceDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PROVIDER, provider)
                }
            }
        }
    }
}