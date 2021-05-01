package at.shockbytes.dante.ui.fragment.dialog

import android.content.Context
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.DialogfragmentApproveImportBinding
import at.shockbytes.dante.importer.ImportStats
import at.shockbytes.dante.util.DanteUtils.dpToPixelF
import at.shockbytes.dante.util.layoutInflater
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

class ImportApprovalDialogFragmentWrapper(
    private val providerNameRes: Int,
    private val providerIconRes: Int,
    private val stats: ImportStats.Success
) {

    private var applyListener: (() -> Unit)? = null
    private var dismissListener: (() -> Unit)? = null

    fun setOnApplyListener(listener: () -> Unit): ImportApprovalDialogFragmentWrapper {
        applyListener = listener
        return this
    }

    fun setOnDismissListener(listener: () -> Unit): ImportApprovalDialogFragmentWrapper {
        return this.apply {
            dismissListener = listener
        }
    }

    fun show(context: Context) {

        val vb = DialogfragmentApproveImportBinding.inflate(context.layoutInflater())

        val dialog = MaterialDialog(context)
            .customView(view = vb.root)
            .cornerRadius(context.dpToPixelF(6))
            .cancelOnTouchOutside(true)

        setTitle(vb, context, providerNameRes, stats.importedBooks)
        setIcon(vb, providerIconRes)
        setImportStats(vb, context, stats.readLaterBooks, stats.currentlyReadingBooks, stats.readBooks)
        setClickListeners(vb, dialog)

        dialog.show()
    }

    private fun setIcon(
        vb: DialogfragmentApproveImportBinding,
        providerIconRes: Int
    ) {
        vb.ivApproveImportIcon.setImageResource(providerIconRes)
    }

    private fun setTitle(
        vb: DialogfragmentApproveImportBinding,
        context: Context,
        providerNameRes: Int,
        importedBooks: Int
    ) {
        vb.tvApproveImportProvider.text = context.getString(
            R.string.import_approval_title,
            importedBooks,
            context.getString(providerNameRes)
        )
    }

    private fun setImportStats(
        vb: DialogfragmentApproveImportBinding,
        context: Context,
        readLaterBooks: Int,
        currentlyReadingBooks: Int,
        readBooks: Int
    ) {
        vb.tvApproveImportReadLater.text = context.getString(R.string.import_read_later, readLaterBooks)
        vb.tvApproveImportCurrent.text = context.getString(R.string.import_reading, currentlyReadingBooks)
        vb.tvApproveImportRead.text = context.getString(R.string.import_read, readBooks)
    }

    private fun setClickListeners(
        vb: DialogfragmentApproveImportBinding,
        dialog: MaterialDialog
    ) {
        vb.btnApproveImportCancel.setOnClickListener {
            dismissListener?.invoke()
            dialog.dismiss()
        }
        vb.btnApproveImportImport.setOnClickListener {
            applyListener?.invoke()
            dialog.dismiss()
        }
    }

    companion object {

        fun newInstance(
            providerNameRes: Int,
            providerIconRes: Int,
            stats: ImportStats.Success
        ): ImportApprovalDialogFragmentWrapper {
            return ImportApprovalDialogFragmentWrapper(
                providerNameRes,
                providerIconRes,
                stats
            )
        }
    }
}