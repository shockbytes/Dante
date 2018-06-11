package at.shockbytes.dante.ui.activity.core

import android.os.Build
import android.os.Bundle
import android.transition.Explode
import android.transition.Slide
import android.view.Gravity
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.dagger.AppComponent
import com.google.android.material.snackbar.Snackbar


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

    protected fun showToast(text: Int) {
        showToast(getString(text))
    }

    protected fun showToast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    abstract fun injectToGraph(appComponent: AppComponent)

}
