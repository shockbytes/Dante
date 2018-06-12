package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.ui.fragment.MainBookFragment
import at.shockbytes.dante.ui.fragment.SuggestionsFragment


/**
 * @author Martin Macheiner
 * Date: 30.08.2016.
 */
class BookPagerAdapter(private val context: Context,
                       private val enableSuggestions: Boolean = false,
                       fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return if (position < 3) {
            MainBookFragment.newInstance(BookState.values()[position])
        } else {
            SuggestionsFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return if (enableSuggestions) 4 else 3
    }

    override fun getPageTitle(position: Int): CharSequence {

        return when (position) {

            0 -> context.getString(R.string.tab_upcoming)
            1 -> context.getString(R.string.tab_current)
            2 -> context.getString(R.string.tab_done)
            3 -> context.getString(R.string.tab_suggestions)

            else -> "" // Never the case
        }
    }

}
