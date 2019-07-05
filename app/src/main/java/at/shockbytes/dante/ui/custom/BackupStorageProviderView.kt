package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupStorageProvider
import kotlinx.android.synthetic.main.backup_storage_provider_view.view.*

/**
 * Author:  Martin Macheiner
 * Date:    26.05.2019
 */
class BackupStorageProviderView : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        View.inflate(context, R.layout.backup_storage_provider_view, this)
    }

    fun setStorageProvider(backupStorageProvider: BackupStorageProvider, click: ((BackupStorageProvider) -> Unit)? = null) {
        with(backupStorageProvider) {
            iv_backup_storage_provider_icon.setImageResource(icon)
            tv_backup_storage_provider_title.text = title
            tv_backup_storage_provider_rationale.setText(rationale)

            root_backup_storage_provider.setOnClickListener { click?.invoke(this) }
        }
    }
}