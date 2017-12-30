package at.shockbytes.dante.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import at.shockbytes.dante.R;
import at.shockbytes.dante.util.ResourceManager;
import at.shockbytes.dante.util.backup.BackupEntry;
import at.shockbytes.util.adapter.ItemTouchHelperAdapter;
import butterknife.BindView;

/**
 * @author Martin Macheiner
 *         Date: 22.04.2017.
 */

public class BackupEntryAdapter extends BaseAdapter<BackupEntry> implements ItemTouchHelperAdapter {

    public BackupEntryAdapter(Context cxt, List<BackupEntry> data) {
        super(cxt, data);
    }

    @Override
    public BackupEntryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_backup_entry, parent, false));
    }

    @Override
    public boolean onItemMove(int i, int i1) {
        return false;
    }

    @Override
    public void onItemMoveFinished() {

    }

    @Override
    public void onItemDismiss(int i) {

        if (onItemMoveListener != null) {
            onItemMoveListener.onItemDismissed(data.get(i), i);
        }
    }

    class ViewHolder extends BaseAdapter<BackupEntry>.ViewHolder {

        @BindView(R.id.item_backup_entry_imgview_provider)
        ImageView imgViewProvider;

        @BindView(R.id.item_backup_entry_txt_time)
        TextView txtTime;

        @BindView(R.id.item_backup_entry_txt_books)
        TextView txtBookAmount;

        @BindView(R.id.item_backup_entry_txt_device)
        TextView txtDevice;

        ViewHolder(final View itemView) {
            super(itemView);
        }

        @Override
        public void bind(BackupEntry e) {
            content = e;

            if (e.getStorageProvider().equals("gdrive")) {
                imgViewProvider.setImageResource(R.mipmap.ic_google_drive);
            }

            txtTime.setText(ResourceManager.formatTimestamp(e.getTimestamp()));
            txtBookAmount.setText(context.getString(R.string.backup_books_amount, e.getBooks()));
            txtDevice.setText(e.getDevice());
        }

    }


}
