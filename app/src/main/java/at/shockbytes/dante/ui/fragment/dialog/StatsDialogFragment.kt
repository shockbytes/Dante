package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.util.AppParams
import kotterknife.bindView
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 29.08.2016.
 */
class StatsDialogFragment : BaseDialogFragment() {

    @Inject
    protected lateinit var bookManager: BookManager

    private val txtUpcoming: TextView by bindView(R.id.dialogfragment_stats_txt_upcoming)
    private val txtCurrent: TextView by bindView(R.id.dialogfragment_stats_txt_current)
    private val txtDone: TextView by bindView(R.id.dialogfragment_stats_txt_done)
    private val txtPages: TextView by bindView(R.id.dialogfragment_stats_txt_pages)

    private val statsView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_stats, null, false)

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setTitle(R.string.label_stats)
                .setIcon(R.mipmap.ic_launcher)
                .setView(statsView)
                .setCancelable(true)
                .create()
    }

    override fun onResume() {
        super.onResume()
        showStats()
    }

    private fun showStats() {
        bookManager.statistics.subscribe { stats ->
            txtUpcoming.text = getString(R.string.dialogfragment_stats_upcoming, stats[AppParams.statKeyUpcoming])
            txtCurrent.text = getString(R.string.dialogfragment_stats_current, stats[AppParams.statKeyCurrent])
            txtDone.text = getString(R.string.dialogfragment_stats_done, stats[AppParams.statKeyDone])
            txtPages.text = getString(R.string.dialogfragment_stats_pages, stats[AppParams.statKeyPages])
        }
    }

    companion object {

        fun newInstance(): StatsDialogFragment {
            val fragment = StatsDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}
