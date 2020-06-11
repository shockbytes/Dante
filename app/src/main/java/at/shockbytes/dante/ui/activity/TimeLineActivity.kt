package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.ContainerBackNavigableActivity
import at.shockbytes.dante.ui.fragment.TimeLineFragment
import at.shockbytes.dante.util.getStringList
import at.shockbytes.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice

class TimeLineActivity : ContainerBackNavigableActivity() {

    override val displayFragment: Fragment
        get() = TimeLineFragment.newInstance()

    override fun injectToGraph(appComponent: AppComponent) = Unit

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_timeline, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.menu_timeline_sort_by) {
            MaterialDialog(this)
                .title(R.string.dialogfragment_sort_by)
                .message(R.string.timeline_sort_explanation)
                .listItemsSingleChoice(items = getStringList(R.array.sort_timeline) ,initialSelection = 1)
                .icon(R.drawable.ic_timeline_sort)
                .cornerRadius(AppUtils.convertDpInPixel(6, this).toFloat())
                .cancelOnTouchOutside(true)
                .positiveButton(R.string.apply) {
                    it.dismiss()
                    // TODO Inform ViewModel
                }
                .show()
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, TimeLineActivity::class.java)
        }
    }
}