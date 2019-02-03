package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.BackupEntry
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ItemTouchHelperAdapter
import kotterknife.bindView

/**
 * Author:  Martin Macheiner
 * Date:    22.04.2017.
 */
class BackupEntryAdapter(cxt: Context) : BaseAdapter<BackupEntry>(cxt), ItemTouchHelperAdapter {

    var onItemDeleteClickListener: ((BackupEntry, Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseAdapter<BackupEntry>.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_backup_entry, parent, false))
    }

    override fun onItemMove(from: Int, to: Int) = false

    override fun onItemMoveFinished() {}

    override fun onItemDismiss(position: Int) {
        onItemMoveListener?.onItemDismissed(data[position], position)
    }

    internal inner class ViewHolder(itemView: View) : BaseAdapter<BackupEntry>.ViewHolder(itemView) {

        private val imgViewProvider by bindView<ImageView>(R.id.item_backup_entry_imgview_provider)
        private val txtTime by bindView<TextView>(R.id.item_backup_entry_txt_time)
        private val txtBookAmount by bindView<TextView>(R.id.item_backup_entry_txt_books)
        private val txtDevice by bindView<TextView>(R.id.item_backup_entry_txt_device)
        private val imgViewDelete by bindView<ImageView>(R.id.item_backup_entry_btn_delete)

        override fun bindToView(t: BackupEntry) {

            if (t.storageProvider == "gdrive") {
                imgViewProvider.setImageResource(R.drawable.ic_google_drive)
            }

            txtTime.text = DanteUtils.formatTimestamp(t.timestamp)
            txtBookAmount.text = context.getString(R.string.backup_books_amount, t.books)
            txtDevice.text = t.device

            imgViewDelete.setOnClickListener {
                onItemDeleteClickListener?.invoke(t, getLocation(t))
            }
        }
    }
}
