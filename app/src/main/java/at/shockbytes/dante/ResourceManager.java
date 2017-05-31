package at.shockbytes.dante;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

}
