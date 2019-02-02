package at.shockbytes.dante.ui.activity.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.ui.activity.DetailActivity
import at.shockbytes.dante.util.createSharingIntent

object ActivityNavigation {

    sealed class Destination {
        class BookDetail(val id: Long, val title: String): Destination()
        class Share(val bookEntity: BookEntity): Destination()
    }

    fun navigateTo(context: Context?, destination: Destination, transitionBundle: Bundle? = null) {

        context?.let {  ctx ->

            val intent = when (destination) {
                is Destination.BookDetail -> DetailActivity.newIntent(ctx, destination.id, destination.title)
                is Destination.Share -> {
                    Intent.createChooser(
                            destination.bookEntity.createSharingIntent(ctx),
                            ctx.resources.getText(R.string.send_to)
                    )
                }
            }

            ctx.startActivity(intent, transitionBundle)
        }
    }


}