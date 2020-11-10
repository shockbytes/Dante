package at.shockbytes.dante.ui.adapter

import at.shockbytes.dante.backup.model.BackupMetadata

interface OnBackupOverflowItemListener {

    fun onBackupItemDeleted(content: BackupMetadata, location: Int)

    fun onBackupItemDownloadRequest(content: BackupMetadata.WithLocalFile)
}
