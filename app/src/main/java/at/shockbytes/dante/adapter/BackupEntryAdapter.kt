package at.shockbytes.dante.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.BackupEntry
import at.shockbytes.dante.util.ResourceManager
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ItemTouchHelperAdapter
import kotterknife.bindView

/**
 * @author Martin Macheiner
 * Date: 22.04.2017.
 */

class BackupEntryAdapter(cxt: Context, data: List<BackupEntry>)
    : BaseAdapter<BackupEntry>(cxt, data.toMutableList()), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): BaseAdapter<BackupEntry>.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_backup_entry, parent, false))
    }

    override fun onItemMove(from: Int, to: Int) = false

    override fun onItemMoveFinished() {}

    override fun onItemDismiss(position: Int) {
        onItemMoveListener?.onItemDismissed(data[position], position)
    }

    internal inner class ViewHolder(itemView: View) : BaseAdapter<BackupEntry>.ViewHolder(itemView) {

        private val imgViewProvider: ImageView by bindView(R.id.item_backup_entry_imgview_provider)

        private val txtTime: TextView by bindView(R.id.item_backup_entry_txt_time)

        private val txtBookAmount: TextView by bindView(R.id.item_backup_entry_txt_books)

        private val txtDevice: TextView by bindView(R.id.item_backup_entry_txt_device)

        override fun bind(t: BackupEntry) {
            content = t

            if (t.storageProvider == "gdrive") {
                imgViewProvider.setImageResource(R.drawable.ic_google_drive)
            }

            txtTime.text = ResourceManager.formatTimestamp(t.timestamp)
            txtBookAmount.text = context.getString(R.string.backup_books_amount, t.books)
            txtDevice.text = t.device
        }

    }


}
