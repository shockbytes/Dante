package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.annotation.ColorRes
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import io.reactivex.disposables.CompositeDisposable
import at.shockbytes.dante.util.colored

/**
 * Author:  Martin Macheiner
 * Date:    29.11.2017
 */
abstract class BaseFragment<V: ViewBinding> : Fragment() {

    protected val compositeDisposable = CompositeDisposable()

    private var _binding: V? = null
    // This property is only valid between onCreateView and onDestroyView
    protected val vb: V
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectToGraph((activity?.application as DanteApp).appComponent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createViewBinding(inflater, container, false)
        return vb.root
    }

    abstract fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): V

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        action: (Snackbar.() -> Unit)? = null
    ) {
        view?.let { v ->
            val duration = if (showIndefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
            val snackBar = Snackbar.make(v, text, duration)
            snackBar.setAction(actionText) { action?.invoke(snackBar) }
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