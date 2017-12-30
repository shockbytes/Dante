package at.shockbytes.dante.util;

import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import at.shockbytes.dante.R;
import at.shockbytes.dante.util.books.Book;

/**
 * @author Martin Macheiner
 *         Date: 30.04.2017.
 */

public class ResourceManager {

    private static SimpleDateFormat SDF = new SimpleDateFormat("dd. MMM yyy - kk:mm",
                                                                Locale.getDefault());


    public static String formatTimestamp(long timeMillis) {
        return SDF.format(new Date(timeMillis));
    }

    public static Intent createSharingIntent(Context c, Book b) {

        String msg = c.getString(R.string.share_template, b.getTitle(), b.getGoogleBooksLink());
        return new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, msg)
                .setType("text/plain");
    }

}
