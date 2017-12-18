package at.shockbytes.dante.ui.activity

import android.os.Bundle
import android.view.MenuItem

abstract class BackNavigableActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                supportFinishAfterTransition()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
