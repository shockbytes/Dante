package at.shockbytes.dante.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import at.shockbytes.dante.R
import android.content.ComponentName
import at.shockbytes.dante.DanteApp
import timber.log.Timber

class DanteAppWidget : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        Timber.d(intent?.action)

        val shouldUpdate = intent?.getBooleanExtra("should_update", false) ?: false

        if (shouldUpdate) {

            context?.let {
                val ids = AppWidgetManager
                    .getInstance(context)
                    .getAppWidgetIds(ComponentName(context, DanteAppWidget::class.java))

                this.onUpdate(context, AppWidgetManager.getInstance(context), ids)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        (context.applicationContext as DanteApp).appComponent.inject(this)

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {

        Timber.d("Updating app widgets")

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.dante_app_widget)

        val intent = Intent(context, DanteRemoteViewsService::class.java)
        views.setRemoteAdapter(R.id.app_widget_lv_content, intent)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
