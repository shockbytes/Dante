package at.shockbytes.dante.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.settings.DanteSettings
import javax.inject.Inject

class DanteRemoteViewsService : RemoteViewsService() {

    @Inject
    lateinit var bookEntityDao: BookEntityDao

    @Inject
    lateinit var danteSettings: DanteSettings

    override fun onCreate() {
        super.onCreate()
        (applicationContext as DanteApp).appComponent.inject(this)
    }

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return DanteRemoteViewsFactory(this, bookEntityDao, danteSettings)
    }
}