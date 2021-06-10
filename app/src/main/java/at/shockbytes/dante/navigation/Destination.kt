package at.shockbytes.dante.navigation

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import at.shockbytes.dante.R
import at.shockbytes.dante.camera.BarcodeCaptureActivity
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookId
import at.shockbytes.dante.core.createSharingIntent
import at.shockbytes.dante.ui.activity.BookStorageActivity
import at.shockbytes.dante.ui.activity.DetailActivity
import at.shockbytes.dante.ui.activity.LoginActivity
import at.shockbytes.dante.ui.activity.MainActivity
import at.shockbytes.dante.ui.activity.ManualAddActivity
import at.shockbytes.dante.ui.activity.NotesActivity
import at.shockbytes.dante.ui.activity.SearchActivity
import at.shockbytes.dante.ui.activity.SettingsActivity
import at.shockbytes.dante.ui.activity.StatisticsActivity
import at.shockbytes.dante.ui.activity.SuggestionsActivity
import at.shockbytes.dante.ui.activity.TimeLineActivity
import at.shockbytes.dante.ui.activity.WishlistActivity
import kotlinx.parcelize.Parcelize

sealed class Destination {

    abstract fun provideIntent(context: Context): Intent

    data class BookDetail(private val info: BookDetailInfo) : Destination() {

        @Parcelize
        data class BookDetailInfo(
            val id: BookId,
            val title: String
        ) : Parcelable

        override fun provideIntent(context: Context): Intent {
            return DetailActivity.newIntent(context, info.id, info.title)
        }
    }

    data class Share(private val bookEntity: BookEntity) : Destination() {

        override fun provideIntent(context: Context): Intent {
            return Intent.createChooser(
                bookEntity.createSharingIntent(context),
                context.resources.getText(R.string.send_to)
            )
        }
    }

    data class Main(
        private val bookDetailInfo: BookDetail.BookDetailInfo? = null,
        private val openCameraAfterLaunch: Boolean = false
    ) : Destination() {

        override fun provideIntent(context: Context): Intent {
            return MainActivity.newIntent(context, bookDetailInfo, openCameraAfterLaunch)
        }
    }

    object Search : Destination() {

        override fun provideIntent(context: Context): Intent {
            return SearchActivity.newIntent(context)
        }
    }

    data class ManualAdd(private val updatedBookEntity: BookEntity? = null) : Destination() {

        override fun provideIntent(context: Context): Intent {
            return ManualAddActivity.newIntent(context, updatedBookEntity)
        }
    }

    object Statistics : Destination() {

        override fun provideIntent(context: Context): Intent {
            return StatisticsActivity.newIntent(context)
        }
    }

    object Timeline : Destination() {

        override fun provideIntent(context: Context): Intent {
            return TimeLineActivity.newIntent(context)
        }
    }

    object BookStorage : Destination() {

        override fun provideIntent(context: Context): Intent {
            return BookStorageActivity.newIntent(context)
        }
    }

    object Settings : Destination() {

        override fun provideIntent(context: Context): Intent {
            return SettingsActivity.newIntent(context)
        }
    }

    object BarcodeScanner : Destination() {

        override fun provideIntent(context: Context): Intent {
            return BarcodeCaptureActivity.newIntent(context)
        }
    }

    data class Notes(private val notesBundle: NotesBundle) : Destination() {

        override fun provideIntent(context: Context): Intent {
            return NotesActivity.newIntent(context, notesBundle)
        }
    }

    object Wishlist : Destination() {
        override fun provideIntent(context: Context): Intent {
            return WishlistActivity.newIntent(context)
        }
    }

    object Suggestions : Destination() {
        override fun provideIntent(context: Context): Intent {
            return SuggestionsActivity.newIntent(context)
        }
    }

    object Login : Destination() {
        override fun provideIntent(context: Context): Intent {
            return LoginActivity.newIntent(context)
        }
    }
}
