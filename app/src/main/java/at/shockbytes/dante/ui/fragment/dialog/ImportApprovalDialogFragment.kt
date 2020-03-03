package at.shockbytes.dante.ui.fragment.dialog

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.importer.ImportStats
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.util.arguments.argument
import kotterknife.bindView

class ImportApprovalDialogFragment : InteractiveViewDialogFragment<Unit>() {

    private var providerName: String by argument()
    private var stats: ImportStats.Success by argument()

    override val containerView: View
        get() = LayoutInflater.from(context).inflate(R.layout.dialogfragment_approve_import, null, false)

    private val btnImport by bindView<Button>(R.id.btn_approve_import_import)
    private val btnCancel by bindView<Button>(R.id.btn_approve_import_cancel)
    private val tvTitle by bindView<TextView>(R.id.tv_approve_import_provider)
    private val tvReadLater by bindView<TextView>(R.id.tv_approve_import_read_later)
    private val tvReading by bindView<TextView>(R.id.tv_approve_import_current)
    private val tvRead by bindView<TextView>(R.id.tv_approve_import_read)

    override fun setupViews() {
        setTitle(providerName, stats.importedBooks)
        setImportStats(stats.readLaterBooks, stats.currentlyReadingBooks, stats.readBooks)
        setClickListeners()
    }

    private fun setTitle(providerName: String, importedBooks: Int) {
        tvTitle.text = getString(R.string.import_approval_title, importedBooks, providerName)
    }

    private fun setImportStats(readLaterBooks: Int, currentlyReadingBooks: Int, readBooks: Int) {
        // TODO setup this view
    }

    private fun setClickListeners() {
        btnCancel.setOnClickListener {
            onDismissListener?.invoke()
            dismiss()
        }
        btnImport.setOnClickListener {
            applyListener?.invoke(Unit)
            dismiss()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit

    companion object {

        fun newInstance(providerName: String, stats: ImportStats.Success): ImportApprovalDialogFragment {
            return ImportApprovalDialogFragment().apply {
                this.providerName = providerName
                this.stats = stats
            }
        }
    }
}