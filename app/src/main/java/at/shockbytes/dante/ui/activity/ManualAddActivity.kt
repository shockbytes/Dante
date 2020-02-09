package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.ContainerTintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.ManualAddFragment

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
class ManualAddActivity : ContainerTintableBackNavigableActivity() {

    private var bookEntity: BookEntity? = null

    override val displayFragment: Fragment
        get() = ManualAddFragment.newInstance(bookEntity)

    override fun onCreate(savedInstanceState: Bundle?) {

        bookEntity = intent.extras?.getParcelable(ARG_BOOK_ENTITY_UPDATE)

        super.onCreate(savedInstanceState)
    }

    override fun injectToGraph(appComponent: AppComponent) {}

    companion object {

        const val ACTION_BOOK_UPDATED = "action_book_updated"
        const val EXTRA_UPDATED_BOOK_STATE = "extra_updated_book_state"

        private const val ARG_BOOK_ENTITY_UPDATE = "arg_book_entity_update"

        fun newIntent(context: Context, bookEntity: BookEntity? = null): Intent {
            return Intent(context, ManualAddActivity::class.java)
                .putExtra(ARG_BOOK_ENTITY_UPDATE, bookEntity)
        }
    }
}