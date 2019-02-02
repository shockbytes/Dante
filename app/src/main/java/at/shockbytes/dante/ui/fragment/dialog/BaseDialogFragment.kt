package at.shockbytes.dante.ui.fragment.dialog

import android.os.Bundle
import android.support.v4.app.DialogFragment
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.dagger.AppComponent
import io.reactivex.disposables.CompositeDisposable

/**
 * Author: Martin Macheiner
 * Date: 30.12.2017.
 */
abstract class BaseDialogFragment : DialogFragment() {

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