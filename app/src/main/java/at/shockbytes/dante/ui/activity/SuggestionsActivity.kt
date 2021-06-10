package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.ui.activity.core.ContainerActivity
import at.shockbytes.dante.ui.fragment.MainBookFragment
import at.shockbytes.dante.ui.fragment.SuggestionsFragment

class SuggestionsActivity : ContainerActivity() {

    override val displayFragment: Fragment
        get() = SuggestionsFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, SuggestionsActivity::class.java)
    }
}