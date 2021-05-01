package at.shockbytes.dante.ui.fragment.dialog

import android.view.LayoutInflater
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.DialogfragmentApproveImportBinding
import at.shockbytes.dante.importer.ImportStats
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.util.arguments.argument

class ImportApprovalDialogFragment : InteractiveViewDialogFragment<Unit, DialogfragmentApproveImportBinding>() {

    private var providerNameRes: Int by argument()
    private var providerIconRes: Int by argument()
    private var stats: ImportStats.Success by argument()

    override val containerView: View
        get() = LayoutInflater.from(context).inflate(R.layout.dialogfragment_approve_import, null, false)

    override val vb: DialogfragmentApproveImportBinding
        get() = DialogfragmentApproveImportBinding.bind(containerView)

    override fun setupViews() {
        setTitle(providerNameRes, stats.importedBooks)
        setIcon(providerIconRes)
        setImportStats(stats.readLaterBooks, stats.currentlyReadingBooks, stats.readBooks)
        setClickListeners()
    }

    private fun setIcon(providerIconRes: Int) {
        vb.ivApproveImportIcon.setImageResource(providerIconRes)
    }

    private fun setTitle(providerNameRes: Int, importedBooks: Int) {
        vb.tvApproveImportProvider.text = getString(R.string.import_approval_title, importedBooks, getString(providerNameRes))
    }

    private fun setImportStats(readLaterBooks: Int, currentlyReadingBooks: Int, readBooks: Int) {
        vb.tvApproveImportReadLater.text = getString(R.string.import_read_later, readLaterBooks)
        vb.tvApproveImportCurrent.text = getString(R.string.import_reading, currentlyReadingBooks)
        vb.tvApproveImportRead.text = getString(R.string.import_read, readBooks)
    }

    private fun setClickListeners() {
        vb.btnApproveImportCancel.setOnClickListener {
            onDismissListener?.invoke()
            dismiss()
        }
        vb.btnApproveImportImport.setOnClickListener {
            applyListener?.invoke(Unit)
            dismiss()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit

    companion object {

        fun newInstance(
            providerNameRes: Int,
            providerIconRes: Int,
            stats: ImportStats.Success
        ): ImportApprovalDialogFragment {
            return ImportApprovalDialogFragment().apply {
                this.providerNameRes = providerNameRes
                this.providerIconRes = providerIconRes
                this.stats = stats
            }
        }
    }
}