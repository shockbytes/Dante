package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupEntryState
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ItemTouchHelperAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_backup_entry.*

/**
 * Author:  Martin Macheiner
 * Date:    22.04.2017
 */
class BackupEntryAdapter(ctx: Context) : BaseAdapter<BackupEntryState>(ctx), ItemTouchHelperAdapter {

    var onItemDeleteClickListener: ((BackupEntry, Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return when (viewType) {
            VIEW_TYPE_ACTIVE -> ActiveBackupViewHolder(inflater.inflate(R.layout.item_backup_entry, parent, false))
            VIEW_TYPE_INACTIVE -> throw UnsupportedOperationException("Inactive view types are currently not supported!")
            else -> throw IllegalStateException("Viewtype $viewType cannot be resolved!")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position] is BackupEntryState.Active) VIEW_TYPE_ACTIVE else VIEW_TYPE_INACTIVE
    }

    override fun onItemMove(from: Int, to: Int) = false

    override fun onItemMoveFinished() = Unit

    override fun onItemDismiss(position: Int) {
        onItemMoveListener?.onItemDismissed(data[position], position)
    }

    inner class ActiveBackupViewHolder(
        override val containerView: View
    ) : BaseAdapter<BackupEntryState>.ViewHolder(containerView), LayoutContainer {

        override fun bindToView(t: BackupEntryState) {

            with (t.entry) {
                item_backup_entry_imgview_provider.setImageResource(storageProvider.icon)

                item_backup_entry_txt_time.text = DanteUtils.formatTimestamp(timestamp)
                item_backup_entry_txt_books.text = context.getString(R.string.backup_books_amount, books)
                item_backup_entry_txt_device.text = device

                item_backup_entry_btn_delete.setOnClickListener {
                    onItemDeleteClickListener?.invoke(this, getLocation(t))
                }
            }
        }
    }

    companion object {

        private const val VIEW_TYPE_ACTIVE = 0
        private const val VIEW_TYPE_INACTIVE = 1
    }
}
