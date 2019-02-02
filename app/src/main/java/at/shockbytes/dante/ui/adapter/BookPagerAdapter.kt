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
 * Author:  Martin Macheiner
 * Date:    30.08.2016
 */
class BookPagerAdapter(
    private val context: Context,
    private val enableSuggestions: Boolean = false,
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return if (position < COUNT_STANDARD) {
            MainBookFragment.newInstance(BookState.values()[position])
        } else {
            SuggestionsFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return if (enableSuggestions) COUNT_SUGGESTIONS else COUNT_STANDARD
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

    companion object {
        private const val COUNT_STANDARD = 3
        private const val COUNT_SUGGESTIONS = 4
    }
}
