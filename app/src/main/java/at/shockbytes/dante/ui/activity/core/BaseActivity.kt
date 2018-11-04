package at.shockbytes.dante.ui.activity.core

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.transition.Explode
import android.transition.Slide
import android.view.Gravity
import android.view.Window
import android.widget.Toast
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.ManualAddActivity

abstract class BaseActivity : AppCompatActivity() {

    open val enableActivityTransition: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (enableActivityTransition) {
                window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
                window.exitTransition = Slide(Gravity.BOTTOM)
                window.enterTransition = Explode()
            }
        }
        injectToGraph((application as DanteApp).appComponent)
    }

    protected fun showSnackbar(text: String) {
        if (!text.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show()
        }
    }

    protected fun showToast(text: Int, showLong: Boolean = false) {
        showToast(getString(text), showLong)
    }

    protected fun showToast(text: String, showLong: Boolean = false) {
        val length = if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(applicationContext, text, length).show()
    }

    fun startActivityDelayed(intent: Intent, bundle: Bundle?, delay: Long) {
        Handler().postDelayed ( {
            startActivity(intent, bundle)
        }, delay)
    }

    abstract fun injectToGraph(appComponent: AppComponent)

}
