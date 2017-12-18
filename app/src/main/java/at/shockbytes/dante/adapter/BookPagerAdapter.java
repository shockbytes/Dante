package at.shockbytes.dante.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import at.shockbytes.dante.R;
import at.shockbytes.dante.util.books.Book;
import at.shockbytes.dante.ui.fragment.MainBookFragment;

/**
 * @author Martin Macheiner
 *         Date: 30.08.2016.
 */
public class BookPagerAdapter extends FragmentStatePagerAdapter {

    public BookPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return MainBookFragment.newInstance(Book.State.values()[position]);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        String str = "";
        switch (position) {

            case 0:

                str = "Später";
                break;

            case 1:

                str = "Lesen";
                break;

            case 2:

                str = "Gelesen";
                break;
        }
        return str;
    }

    public int getPageIcon(int position) {

        int icon = 0;
        switch (position) {

            case 0:

                icon = R.drawable.ic_tab_upcoming;
                break;

            case 1:

                icon = R.drawable.ic_tab_current;
                break;

            case 2:

                icon = R.drawable.ic_tab_done;
                break;

        }

        return icon;
    }


}
