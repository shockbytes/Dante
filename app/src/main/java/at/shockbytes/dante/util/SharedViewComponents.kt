package at.shockbytes.dante.util

import android.content.Context
import android.content.res.Configuration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import at.shockbytes.dante.R

object SharedViewComponents {

    fun layoutManagerForBooks(context: Context): RecyclerView.LayoutManager {

        val resources = context.resources
        return if (resources.getBoolean(R.bool.isTablet)) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            else
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            else
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }
}