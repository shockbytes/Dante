package at.shockbytes.dante.ui.fragment.dialog

import android.os.Bundle
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.injection.AppComponent
import io.reactivex.disposables.CompositeDisposable

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017
 */
abstract class BaseDialogFragment : androidx.fragment.app.DialogFragment() {

    protected val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectToGraph((activity?.application as DanteApp).appComponent)
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    abstract fun injectToGraph(appComponent: AppComponent)
}