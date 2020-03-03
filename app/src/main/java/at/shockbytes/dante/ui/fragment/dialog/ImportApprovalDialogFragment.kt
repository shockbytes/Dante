package at.shockbytes.dante.ui.fragment.dialog

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.importer.ImportStats
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.util.arguments.argument
import kotlinx.android.synthetic.main.dialogfragment_approve_import.*
import kotterknife.bindView

class ImportApprovalDialogFragment : InteractiveViewDialogFragment<Unit>() {

    private var providerNameRes: Int by argument()
    private var providerIconRes: Int by argument()
    private var stats: ImportStats.Success by argument()

    override val containerView: View
        get() = LayoutInflater.from(context).inflate(R.layout.dialogfragment_approve_import, null, false)

    private val btnImport by bindView<Button>(R.id.btn_approve_import_import)
    private val btnCancel by bindView<Button>(R.id.btn_approve_import_cancel)
    private val tvTitle by bindView<TextView>(R.id.tv_approve_import_provider)
    private val tvReadLater by bindView<TextView>(R.id.tv_approve_import_read_later)
    private val tvReading by bindView<TextView>(R.id.tv_approve_import_current)
    private val tvRead by bindView<TextView>(R.id.tv_approve_import_read)
    private val ivIcon by bindView<ImageView>(R.id.iv_approve_import_icon)

    override fun setupViews() {
        setTitle(providerNameRes, stats.importedBooks)
        setIcon(providerIconRes)
        setImportStats(stats.readLaterBooks, stats.currentlyReadingBooks, stats.readBooks)
        setClickListeners()
    }

    private fun setIcon(providerIconRes: Int) {
        ivIcon.setImageResource(providerIconRes)
    }

    private fun setTitle(providerNameRes: Int, importedBooks: Int) {
        tvTitle.text = getString(R.string.import_approval_title, importedBooks, getString(providerNameRes))
    }

    private fun setImportStats(readLaterBooks: Int, currentlyReadingBooks: Int, readBooks: Int) {
        tvReadLater.text = getString(R.string.import_read_later, readLaterBooks)
        tvReading.text = getString(R.string.import_reading, currentlyReadingBooks)
        tvRead.text = getString(R.string.import_read, readBooks)
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