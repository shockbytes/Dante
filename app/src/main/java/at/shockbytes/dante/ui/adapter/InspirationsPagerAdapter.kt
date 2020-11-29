package at.shockbytes.dante.ui.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import at.shockbytes.dante.R
import at.shockbytes.dante.ui.fragment.SuggestionsFragment
import at.shockbytes.dante.ui.fragment.WishlistFragment

class InspirationsPagerAdapter(
    private val context: Context,
    fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int = PAGE_COUNT

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> context.getString(R.string.wishlist_title)
            1 -> context.getString(R.string.suggestions_title)
            else -> throw IllegalStateException("Position $position out of bounds of InspirationsPagerAdapter!")
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> WishlistFragment.newInstance()
            1 -> SuggestionsFragment.newInstance()
            else -> throw IllegalStateException("Position $position out of bounds of InspirationsPagerAdapter!")
        }
    }

    companion object {
        private const val PAGE_COUNT = 2
    }
}
