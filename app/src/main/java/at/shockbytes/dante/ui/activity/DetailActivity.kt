package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.BookDetailFragment

class DetailActivity : TintableBackNavigableActivity() {

    private var detailFragment: BookDetailFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getLongExtra(ARG_ID, -1)
        val title = intent.getStringExtra(ARG_TITLE)

        supportActionBar?.title = title

        detailFragment = BookDetailFragment.newInstance(id)
                .also {
                    supportFragmentManager.beginTransaction()
                            .replace(android.R.id.content, it)
                            .commit()
                }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        // Do nothing...
    }

    override fun backwardAnimation() {
        super.backwardAnimation()
        detailFragment?.backwardAnimation()
    }


    companion object {

        private const val ARG_ID = "arg_id"
        private const val ARG_TITLE = "arg_title"

        fun newIntent(context: Context, id: Long, title: String): Intent {
            return Intent(context, DetailActivity::class.java)
                    .putExtra(ARG_ID, id)
                    .putExtra(ARG_TITLE, title)
        }
    }

}
