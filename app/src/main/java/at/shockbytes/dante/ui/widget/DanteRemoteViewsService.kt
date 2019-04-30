package at.shockbytes.dante.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.data.BookEntityDao
import javax.inject.Inject

class DanteRemoteViewsService : RemoteViewsService() {

    @Inject
    lateinit var bookEntityDao: BookEntityDao

    override fun onCreate() {
        super.onCreate()
        (applicationContext as DanteApp).appComponent.inject(this)
    }

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return DanteRemoteViewsFactory(this, bookEntityDao)
    }
}