package at.shockbytes.dante.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import at.shockbytes.dante.R
import android.content.ComponentName
import at.shockbytes.dante.DanteApp
import android.app.PendingIntent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import at.shockbytes.dante.core.book.BookIds
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.navigation.Destination

class DanteAppWidget : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        when (intent?.action) {
            ACTION_BOOK_CLICKED -> handleOnBookClickedEvent(context, intent)
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> handleAppWidgetUpdate(context, intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        (context.applicationContext as DanteApp).appComponent.inject(this)

        // There may be multiple widgets active, so update all of them
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) = Unit
    override fun onDisabled(context: Context) = Unit

    private fun handleOnBookClickedEvent(context: Context?, intent: Intent) {

        val bookId = intent.getLongExtra(EXTRA_BOOK_ID, BookIds.default())
        val bookTitle = intent.getStringExtra(EXTRA_BOOK_TITLE) ?: ""
        ActivityNavigator.navigateTo(
            context,
            Destination.Main(Destination.BookDetail.BookDetailInfo(bookId, bookTitle)),
            intentFlags = FLAG_ACTIVITY_NEW_TASK
        )
    }

    private fun handleAppWidgetUpdate(context: Context?, intent: Intent) {
        val shouldUpdate = intent.getBooleanExtra("should_update", false)

        if (shouldUpdate) {

            context?.let {
                val ids = AppWidgetManager
                    .getInstance(context)
                    .getAppWidgetIds(ComponentName(context, DanteAppWidget::class.java))

                this.onUpdate(context, AppWidgetManager.getInstance(context), ids)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.dante_app_widget)

        val intent = Intent(context, DanteRemoteViewsService::class.java)
        views.setRemoteAdapter(R.id.app_widget_lv_content, intent)

        views.setPendingIntentTemplate(R.id.app_widget_lv_content, createPendingIntentTemplate(context))

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.app_widget_lv_content)
    }

    private fun createPendingIntentTemplate(context: Context): PendingIntent {
        val openIntent = Intent(context, this::class.java).setAction(ACTION_BOOK_CLICKED)
        return PendingIntent.getBroadcast(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {

        const val EXTRA_BOOK_ID = "extra_book_id"
        const val EXTRA_BOOK_TITLE = "extra_book_title"
        private const val ACTION_BOOK_CLICKED = "action_book_clicked"
    }
}
