package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.databinding.BackupStorageProviderViewBinding
import at.shockbytes.dante.util.Stability
import at.shockbytes.dante.util.setVisible

/**
 * Author:  Martin Macheiner
 * Date:    26.05.2019
 */
class BackupStorageProviderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val vb = BackupStorageProviderViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    fun setStorageProvider(backupStorageProvider: BackupStorageProvider, click: ((BackupStorageProvider) -> Unit)? = null) {
        with(backupStorageProvider) {
            vb.ivBackupStorageProviderIcon.setImageResource(icon)
            vb.tvBackupStorageProviderTitle.text = title
            vb.tvBackupStorageProviderRationale.setText(rationale)

            vb.rootBackupStorageProvider.setOnClickListener { click?.invoke(this) }
            vb.tvBackupItemBeta.setVisible(backupStorageProvider.stability == Stability.BETA)
            vb.tvBackupItemDiscontinued.setVisible(backupStorageProvider.stability == Stability.DISCONTINUED)
        }
    }
}