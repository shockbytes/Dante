package at.shockbytes.dante.ui.widget

import android.content.Context
import android.appwidget.AppWidgetManager
import android.content.Intent

object DanteAppWidgetManager {

    fun refresh(context: Context) {

        context.sendBroadcast(
            Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                putExtra("should_update", true)
            }
        )
    }
}