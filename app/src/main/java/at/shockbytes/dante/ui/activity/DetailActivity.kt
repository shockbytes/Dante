package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.BackAnimatable
import at.shockbytes.dante.ui.fragment.BookDetailFragment
import at.shockbytes.dante.ui.fragment.LegacyBookDetailFragment
import at.shockbytes.dante.util.flagging.FeatureFlag
import at.shockbytes.dante.util.flagging.FeatureFlagging
import javax.inject.Inject

class DetailActivity : TintableBackNavigableActivity() {

    @Inject
    lateinit var featureFlagging: FeatureFlagging

    private var detailFragment: BackAnimatable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getLongExtra(ARG_ID, -1)
        val title = intent.getStringExtra(ARG_TITLE)

        supportActionBar?.title = title

        detailFragment = pickDetailFragment(id)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun backwardAnimation() {
        super.backwardAnimation()
        performBackwardAnimation()
    }

    override fun onBackStackPopped() {
        super.onBackStackPopped()
        performBackwardAnimation()
    }

    private fun performBackwardAnimation() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            detailFragment?.onBackwardAnimation()
        }
    }

    private fun pickDetailFragment(id: Long): BackAnimatable {

        val fragment: Fragment = if (featureFlagging[FeatureFlag.UpdatedDetailPage]) {
            BookDetailFragment.newInstance(id)
        } else {
            LegacyBookDetailFragment.newInstance(id)
        }

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit()

        return fragment as BackAnimatable // This cast is fine, both implement this interface
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
