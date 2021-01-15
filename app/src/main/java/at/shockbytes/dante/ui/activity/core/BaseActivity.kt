package at.shockbytes.dante.ui.activity.core

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import io.reactivex.disposables.CompositeDisposable

abstract class BaseActivity<V: ViewBinding> : AppCompatActivity() {

    protected open val activityTransition: ActivityTransition? = ActivityTransition.default()

    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()

    protected lateinit var vb: V

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activityTransition?.let { at ->
                window.exitTransition = at.exitTransition
                window.enterTransition = at.enterTransition
            }
        }
        injectToGraph((application as DanteApp).appComponent)
        setupActionBar()
        super.onCreate(savedInstanceState)
    }

    fun setContentViewWithBinding(creator: (layoutInflater: LayoutInflater) -> V) {
        vb = creator(layoutInflater)
        setContentView(vb.root)
    }

    open fun setupActionBar() {
        supportActionBar?.apply {
            elevation = 0f
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@BaseActivity, R.color.mainBackground)))
        }
    }

    protected fun showSnackbar(text: String) {
        if (text.isNotEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    protected fun showToast(text: Int, showLong: Boolean = false) {
        showToast(getString(text), showLong)
    }

    protected fun showToast(text: String, showLong: Boolean = false) {
        val length = if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(applicationContext, text, length).show()
    }

    fun startActivityDelayed(intent: Intent, bundle: Bundle?, delay: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent, bundle)
        }, delay)
    }

    abstract fun injectToGraph(appComponent: AppComponent)
}
