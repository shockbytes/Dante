package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.dagger.AppComponent

/**
 * @author Martin Macheiner
 * Date: 29.11.2017.
 */
abstract class BaseFragment : Fragment() {

    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectToGraph((activity?.application as DanteApp).appComponent)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    protected abstract fun setupViews()

    protected abstract fun injectToGraph(appComponent: AppComponent)

    @JvmOverloads
    protected fun showSnackbar(text: String, actionText: String,
                               showIndefinite: Boolean = false, action: Snackbar.() -> Unit) {
        view?.let { v ->
            val duration = if (showIndefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
            val snackBar = Snackbar.make(v, text, duration)
            snackBar.setAction(actionText) { action(snackBar) }
            snackBar.show()
        }
    }

    @JvmOverloads
    protected fun showSnackbar(text: String, showLong: Boolean = true) {
        view?.let { v ->
            val duration = if (showLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
            Snackbar.make(v, text, duration).show()
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

}