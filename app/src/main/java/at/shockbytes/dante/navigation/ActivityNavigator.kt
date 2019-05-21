package at.shockbytes.dante.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.ui.activity.BackupActivity
import at.shockbytes.dante.ui.activity.BookRetrievalActivity
import at.shockbytes.dante.ui.activity.DetailActivity
import at.shockbytes.dante.ui.activity.MainActivity
import at.shockbytes.dante.ui.activity.ManualAddActivity
import at.shockbytes.dante.ui.activity.SearchActivity
import at.shockbytes.dante.ui.activity.SettingsActivity
import at.shockbytes.dante.ui.activity.StatisticsActivity
import at.shockbytes.dante.util.createSharingIntent
import kotlinx.android.parcel.Parcelize

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
                is Destination.Retrieval -> BookRetrievalActivity.newIntent(context, destination.type, destination.query)
                is Destination.Main -> MainActivity.newIntent(context, destination.bookDetailInfo, destination.openCameraAfterLaunch)
                is Destination.ManualAdd -> ManualAddActivity.newIntent(context)
                is Destination.Statistics -> StatisticsActivity.newIntent(context)
                is Destination.Backup -> BackupActivity.newIntent(context)
                is Destination.Settings -> SettingsActivity.newIntent(context)
            }

            intentFlags?.let { flags ->
                intent.flags = flags
            }

            ctx.startActivity(intent, transitionBundle)
        }
    }
}