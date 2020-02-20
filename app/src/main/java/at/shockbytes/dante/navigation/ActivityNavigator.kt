package at.shockbytes.dante.navigation

import android.content.Context
import android.os.Bundle

object ActivityNavigator {

    fun navigateTo(
        context: Context?,
        destination: Destination,
        transitionBundle: Bundle? = null,
        intentFlags: Int? = null
    ) {

        context?.let { ctx ->

            val intent = destination.provideIntent(ctx)
            intentFlags?.let(intent::setFlags)

            ctx.startActivity(intent, transitionBundle)
        }
    }
}