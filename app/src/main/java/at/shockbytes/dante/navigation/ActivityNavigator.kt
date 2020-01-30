package at.shockbytes.dante.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.camera.BarcodeCaptureActivity
import at.shockbytes.dante.ui.activity.BackupActivity
import at.shockbytes.dante.ui.activity.DetailActivity
import at.shockbytes.dante.ui.activity.MainActivity
import at.shockbytes.dante.ui.activity.ManualAddActivity
import at.shockbytes.dante.ui.activity.SearchActivity
import at.shockbytes.dante.ui.activity.SettingsActivity
import at.shockbytes.dante.ui.activity.StatisticsActivity
import at.shockbytes.dante.core.createSharingIntent
import at.shockbytes.dante.ui.activity.NotesActivity
import at.shockbytes.dante.ui.activity.TimeLineActivity

object ActivityNavigator {

    fun navigateTo(
        context: Context?,
        destination: Destination,
        transitionBundle: Bundle? = null,
        intentFlags: Int? = null
    ) {

        context?.let { ctx ->
            val intent = when (destination) {
                is Destination.BookDetail -> DetailActivity.newIntent(ctx, destination.info.id, destination.info.title)
                is Destination.Share -> {
                    Intent.createChooser(
                            destination.bookEntity.createSharingIntent(ctx),
                            ctx.resources.getText(R.string.send_to)
                    )
                }
                is Destination.Search -> SearchActivity.newIntent(context)
                is Destination.Main -> MainActivity.newIntent(context, destination.bookDetailInfo, destination.openCameraAfterLaunch)
                is Destination.ManualAdd -> ManualAddActivity.newIntent(context)
                is Destination.Statistics -> StatisticsActivity.newIntent(context)
                is Destination.Timeline -> TimeLineActivity.newIntent(context)
                is Destination.Backup -> BackupActivity.newIntent(context)
                is Destination.Settings -> SettingsActivity.newIntent(context)
                Destination.BarcodeScanner -> BarcodeCaptureActivity.newIntent(context)
                is Destination.Notes -> NotesActivity.newIntent(context, destination.notesBundle)
            }

            intentFlags?.let { flags ->
                intent.flags = flags
            }

            ctx.startActivity(intent, transitionBundle)
        }
    }
}