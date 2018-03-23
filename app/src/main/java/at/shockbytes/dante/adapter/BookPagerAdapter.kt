package at.shockbytes.dante.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.books.BookListener
import at.shockbytes.dante.ui.fragment.MainBookFragment
import at.shockbytes.dante.util.books.Book



/**
 * @author Martin Macheiner
 * Date: 30.08.2016.
 */
class BookPagerAdapter(private val context: Context,
                       fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    @Volatile
    var listener: BookListener? = null

    override fun getItem(position: Int): Fragment {
        return MainBookFragment.newInstance(Book.State.values()[position])
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        super.setPrimaryItem(container, position, obj)
        if (listener !== obj) {
            val frag = obj as MainBookFragment
            listener = frag
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {

        return when (position) {

            0 -> context.getString(R.string.tab_upcoming)
            1 -> context.getString(R.string.tab_current)
            2 -> context.getString(R.string.tab_done)

            else -> "" // Never the case
        }
    }

    fun getPageIcon(position: Int): Int {

        return when (position) {

            0 -> R.drawable.ic_tab_upcoming
            1 -> R.drawable.ic_tab_current
            2 -> R.drawable.ic_tab_done

            else -> 0 // Never the case
        }
    }


}
