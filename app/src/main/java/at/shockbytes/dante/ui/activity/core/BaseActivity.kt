package at.shockbytes.dante.ui.activity.core

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.transition.Explode
import android.transition.Fade
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Window
import android.widget.Toast
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.injection.AppComponent
import io.reactivex.disposables.CompositeDisposable

abstract class BaseActivity : AppCompatActivity() {

    protected open val enableActivityTransition: Boolean = true

    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (enableActivityTransition) {
                window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
                window.exitTransition = Fade()
                window.enterTransition = Explode()
            }
        }
        injectToGraph((application as DanteApp).appComponent)
        super.onCreate(savedInstanceState)
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
        Handler().postDelayed({
            startActivity(intent, bundle)
        }, delay)
    }

    abstract fun injectToGraph(appComponent: AppComponent)
}
