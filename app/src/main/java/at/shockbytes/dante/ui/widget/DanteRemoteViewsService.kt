package at.shockbytes.dante.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.util.settings.DanteSettings
import javax.inject.Inject

class DanteRemoteViewsService : RemoteViewsService() {

    @Inject
    lateinit var bookRepository: BookRepository

    @Inject
    lateinit var danteSettings: DanteSettings

    override fun onCreate() {
        super.onCreate()
        (applicationContext as DanteApp).appComponent.inject(this)
    }

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return DanteRemoteViewsFactory(this, bookRepository, danteSettings)
    }
}