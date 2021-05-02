package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.databinding.ItemBackupEntryBinding
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

/**
 * Author:  Martin Macheiner
 * Date:    22.04.2017
 */
class BackupEntryAdapter(
    ctx: Context,
    onItemClickListener: OnItemClickListener<BackupMetadataState>,
    private val onItemOverflowMenuClickedListener: OnBackupOverflowItemListener
) : BaseAdapter<BackupMetadataState>(ctx, onItemClickListener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<BackupMetadataState> {
        return BackupViewHolder(ItemBackupEntryBinding.inflate(inflater, parent, false))
    }

    fun updateData(freshData: List<BackupMetadataState>) {
        data.clear()
        data.addAll(freshData)
        notifyDataSetChanged()
    }

    inner class BackupViewHolder(
        val vb: ItemBackupEntryBinding
    ) : BaseAdapter.ViewHolder<BackupMetadataState>(vb.root) {

        override fun bindToView(content: BackupMetadataState, position: Int) {

            with(content.entry) {
                vb.itemBackupEntryImgviewProvider.setImageResource(storageProvider.icon)

                vb.itemBackupEntryTxtTime.text = DanteUtils.formatTimestamp(timestamp)
                vb.itemBackupEntryTxtBooks.text = context.getString(R.string.books_amount, books)
                vb.itemBackupEntryTxtDevice.text = device

                if (content is BackupMetadataState.Active) {

                    vb.itemBackupEntryCard.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    vb.itemBackupEntryBtnOverflow.setVisible(true)
                    setupOverflowMenu(content)
                } else {
                    vb.itemBackupEntryCard.setBackgroundColor(ContextCompat.getColor(context, R.color.disabled_view))

                    vb.itemBackupEntryBtnOverflow.apply {
                            visibility = View.INVISIBLE
                            setOnClickListener(null)
                    }
                }
            }
        }

        private fun setupOverflowMenu(content: BackupMetadataState) {

            val entry = content.entry
            val popupMenu = PopupMenu(context, vb.itemBackupEntryBtnOverflow)

            popupMenu.menuInflater.inflate(R.menu.menu_backup_item_overflow, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_backup_delete -> {
                        onItemOverflowMenuClickedListener.onBackupItemDeleted(entry, getLocation(content))
                    }
                    R.id.menu_backup_export_request -> {
                        if (entry is BackupMetadata.WithLocalFile) {
                            onItemOverflowMenuClickedListener.onBackupItemDownloadRequest(entry)
                        }
                    }
                    R.id.menu_backup_open_request -> {
                        if (entry is BackupMetadata.WithLocalFile) {
                            onItemOverflowMenuClickedListener.onBackupItemOpenFileRequest(entry)
                        }
                    }
                }
                true
            }

            val showExportOption = content.isFileExportable && entry is BackupMetadata.WithLocalFile
            popupMenu.menu.findItem(R.id.menu_backup_open_request)?.isVisible = showExportOption
            popupMenu.menu.findItem(R.id.menu_backup_export_request)?.isVisible = showExportOption

            val menuHelper = MenuPopupHelper(context, popupMenu.menu as MenuBuilder, vb.itemBackupEntryBtnOverflow)
            menuHelper.setForceShowIcon(true)

            vb.itemBackupEntryBtnOverflow.setOnClickListener { menuHelper.show() }
        }
    }
}
