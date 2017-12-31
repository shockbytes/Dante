package at.shockbytes.dante.ui.activity

import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.transition.Explode
import android.transition.Slide
import android.view.Gravity
import android.view.Window
import android.widget.Toast
import at.shockbytes.dante.core.DanteApp
import at.shockbytes.dante.dagger.AppComponent
import butterknife.ButterKnife
import butterknife.Unbinder

abstract class BaseActivity : AppCompatActivity() {

    open val enableActivityTransition: Boolean = true

    private var unbinder: Unbinder? = null

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

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        unbinder = ButterKnife.bind(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbinder?.unbind()
    }

    protected fun showSnackbar(text: String) {
        if (!text.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show()
        }
    }

    protected fun showToast(text: Int) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    abstract fun injectToGraph(appComponent: AppComponent)

}
