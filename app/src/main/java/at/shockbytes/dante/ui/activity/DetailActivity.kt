package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.ViewCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookId
import at.shockbytes.dante.core.book.BookIds
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.BackAnimatable
import at.shockbytes.dante.ui.fragment.BookDetailFragment
import at.shockbytes.dante.flagging.FeatureFlagging
import at.shockbytes.dante.ui.activity.core.ActivityTransition
import com.google.android.material.transition.platform.MaterialArcMotion
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import java.util.Locale
import javax.inject.Inject

class DetailActivity : TintableBackNavigableActivity() {

    @Inject
    lateinit var featureFlagging: FeatureFlagging

    private var detailFragment: BackAnimatable? = null

    override val activityTransition = ActivityTransition.none()

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSharedElementTransition()
        super.onCreate(savedInstanceState)

        val id = intent.getLongExtra(ARG_ID, BookIds.default())
        val title = intent.getStringExtra(ARG_TITLE)

        if (id != BookIds.default()) {
            supportActionBar?.title = title?.toUpperCase(Locale.getDefault())
            detailFragment = pickDetailFragment(id)
        } else {
            supportFinishAfterTransition()
        }
    }

    private fun setupSharedElementTransition() {
        ViewCompat.setTransitionName(
            findViewById(android.R.id.content),
            "test" // TODO
        )

        // Set up shared element transition and disable overlay so views don't show above system bars
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        val materialTransform = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 450
            pathMotion = MaterialArcMotion()
            scrimColor = Color.TRANSPARENT
        }

        window.sharedElementEnterTransition = materialTransform
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

    private fun pickDetailFragment(id: BookId): BackAnimatable {

        val fragment = BookDetailFragment.newInstance(id)

        supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(android.R.id.content, fragment)
                .commit()

        return fragment
    }

    companion object {

        private const val ARG_ID = "arg_id"
        private const val ARG_TITLE = "arg_title"

        fun newIntent(context: Context, id: BookId, title: String): Intent {
            return Intent(context, DetailActivity::class.java)
                    .putExtra(ARG_ID, id)
                    .putExtra(ARG_TITLE, title)
        }
    }
}
