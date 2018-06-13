package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

/**
 * @author  Martin Macheiner
 * Date:    12.06.2018
 */
abstract class BaseViewModel: ViewModel() {

    protected val compositeDisposable = CompositeDisposable()

    abstract fun poke()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}