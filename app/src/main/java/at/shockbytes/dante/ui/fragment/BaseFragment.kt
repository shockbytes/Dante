package at.shockbytes.remote.fragment


import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.View
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.Unbinder

abstract class BaseFragment : Fragment() {

    private var unbinder: Unbinder? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unbinder = ButterKnife.bind(this, view)
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder?.unbind()
    }

    protected abstract fun setupViews()

    @JvmOverloads
    protected fun showSnackbar(text: String, actionText: String,
                               showIndefinite: Boolean = false,  action: () -> Unit) {
        if (view != null) {
            val duration = if (showIndefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
            Snackbar.make(view!!, text, duration)
                    .setAction(actionText) {
                        action()
                    }.show()
        }
    }

    @JvmOverloads
    protected fun showSnackbar(text: String, showLong: Boolean = true) {
        if (view != null) {
            val duration = if (showLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
            Snackbar.make(view!!, text, duration).show()
        }
    }

    @JvmOverloads
    protected fun showToast(text: String, showLong: Boolean = true) {
        val duration = if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(context, text, duration).show()
    }

}
