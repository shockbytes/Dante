package at.shockbytes.dante.ui.activity.core

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.annotation.DrawableRes
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import at.shockbytes.dante.R
import at.shockbytes.dante.util.DanteUtils

abstract class BackNavigableActivity : BaseActivity() {

    protected var upIndicator: Int = R.drawable.ic_back

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            val drawable = DanteUtils.vector2Drawable(applicationContext, upIndicator)
            drawable.setColorFilter(ContextCompat.getColor(this@BackNavigableActivity, R.color.actionBarItemColor), PorterDuff.Mode.SRC_IN)
            setHomeAsUpIndicator(drawable)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            back()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        back()
    }

    private fun back() {
        backwardAnimation()
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            onBackStackPopped()
        } else {
            super.onBackPressed()
        }
    }

    open fun backwardAnimation() = Unit

    open fun onBackStackPopped() = Unit

    fun setHomeAsUpIndicator(@DrawableRes indicator: Int) {
        supportActionBar?.setHomeAsUpIndicator(indicator)
    }
}
