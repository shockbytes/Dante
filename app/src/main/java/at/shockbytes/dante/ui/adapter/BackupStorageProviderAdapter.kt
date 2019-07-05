package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.ViewGroup
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.ui.custom.BackupStorageProviderView
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

/**
 * Author:  Martin Macheiner
 * Date:    11.06.2019
 */
class BackupStorageProviderAdapter(
    context: Context
) : BaseAdapter<BackupStorageProvider>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BackupStorageProviderView(context))
    }

    fun updateData(providers: List<BackupStorageProvider>) {
        data.clear()
        data.addAll(providers)
    }

    inner class ViewHolder(
        override val containerView: BackupStorageProviderView
    ) : BaseAdapter<BackupStorageProvider>.ViewHolder(containerView), LayoutContainer {

        override fun bindToView(t: BackupStorageProvider) {
            containerView.setStorageProvider(t) {
                onItemClickListener?.onItemClick(t, containerView)
            }
        }
    }
}
