package at.shockbytes.dante.ui.activity.core

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

object ActivityNavigation {

    sealed class Destination {
        class BookDetail(val info: BookDetailInfo) : Destination() {

            @Parcelize
            data class BookDetailInfo(
                val id: Long,
                val title: String
            ) : Parcelable
        }
        class Share(val bookEntity: BookEntity) : Destination()
        class Retrieval(
            val type: BookRetrievalActivity.RetrievalType,
            val query: String?
        ) : Destination()
        class Main(
            val bookDetailInfo: BookDetail.BookDetailInfo? = null,
            val openCameraAfterLaunch: Boolean = false
        ) : Destination()

        object Search : Destination()
        object ManualAdd : Destination()
        object Statistics : Destination()
        object Backup : Destination()
        object Settings : Destination()
    }

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
                ActivityNavigation.Destination.ManualAdd -> ManualAddActivity.newIntent(context)
                ActivityNavigation.Destination.Statistics -> StatisticsActivity.newIntent(context)
                ActivityNavigation.Destination.Backup -> BackupActivity.newIntent(context)
                ActivityNavigation.Destination.Settings -> SettingsActivity.newIntent(context)
            }

            intentFlags?.let { flags ->
                intent.flags = flags
            }

            ctx.startActivity(intent, transitionBundle)
        }
    }
}