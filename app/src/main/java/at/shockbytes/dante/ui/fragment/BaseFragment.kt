package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import io.reactivex.disposables.CompositeDisposable
import at.shockbytes.dante.util.colored

/**
 * Author: Martin Macheiner
 * Date: 29.11.2017
 */
abstract class BaseFragment : Fragment() {

    abstract val layoutId: Int

    protected val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectToGraph((activity?.application as DanteApp).appComponent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    protected abstract fun setupViews()

    protected abstract fun injectToGraph(appComponent: AppComponent)

    protected abstract fun bindViewModel()

    protected abstract fun unbindViewModel()

    @JvmOverloads
    protected fun showSnackbar(
        text: String,
        actionText: String,
        showIndefinite: Boolean = false,
        action: Snackbar.() -> Unit
    ) {
        view?.let { v ->
            val duration = if (showIndefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
            val snackBar = Snackbar.make(v, text, duration)
            snackBar.setAction(actionText) { action(snackBar) }
            snackBar.show()
        }
    }

    @JvmOverloads
    protected fun showSnackbar(
        text: String,
        showLong: Boolean = true,
        @ColorRes fgColor: Int = R.color.snackbarForeground,
        @ColorRes bgColor: Int = R.color.snackbarBackground
    ) {
        view?.let { v ->

            val sbText = text.colored(ContextCompat.getColor(v.context, fgColor))
            val duration = if (showLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT

            val snackbar = Snackbar.make(v, sbText, duration)
            snackbar.view.setBackgroundColor(ContextCompat.getColor(v.context, bgColor))
            snackbar.show()
        }
    }

    @JvmOverloads
    protected fun showToast(text: String, showLong: Boolean = true) {
        val duration = if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(context, text, duration).show()
    }

    @JvmOverloads
    protected fun showToast(text: Int, showLong: Boolean = true) {
        showToast(getString(text), showLong)
    }

    override fun onPause() {
        unbindViewModel()
        compositeDisposable.clear()
        super.onPause()
    }

    override fun onResume() {
        bindViewModel()
        super.onResume()
    }
}