package at.shockbytes.dante.util.tracking;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import at.shockbytes.dante.util.books.Book;
import io.keen.client.android.AndroidKeenClientBuilder;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenProject;

/**
 * @author Martin Macheiner
 *         Date: 01.06.2017.
 */

@SuppressWarnings("unchecked")
public class KeenTracker implements Tracker {

    private static final String PROJECT_ID = "592eb8de95cfc93b3abe8c33";
    private static final String READ_KEY = "D067FCFDB2B7D90D34DE0EA4E914C5B8FF695F595A00C07457519F09C8E69A60080CE16B81A868C227579683CBD1EEA6592FFEF2D729E8F746A0920AA997DB999250A503188894C6B82BE711E790F73A3852FEED4DC7EA414558E75B68DA4603";
    private static final String WRITE_KEY = "0B74F581F4EF0B5424333A41087800CF1CFE15D138D13BAD384B3ED66FB1F32FB725E096D872278284C7A89DF532EF58ED1B9E694D1D20EB6F0CE3A02BBF91466DF75A9FB7A2BCDD5E73916F31CBF9602BF546ADD495C89563FDA1383A267F0B";

    @Inject
    public KeenTracker(@NonNull Context context) {
        initializeKeen(context);
    }

    @Override
    public void trackOnScanBook() {
        trackEvent("bookScan", createTrackEventData(new Pair<String, Object>("scan_clicked", 1)));
    }

    @Override
    public void trackOnScanBookCanceled() {
        trackEvent("bookScanCanceled", createTrackEventData(new Pair<String, Object>("scan_canceled", 1)));
    }

    @Override
    public void trackOnBookManuallyEntered() {
        trackEvent("bookScanManuallyEntered", createTrackEventData(new Pair<String, Object>("book_manually_entered", 1)));
    }

    @Override
    public void trackOnFoundBookCanceled() {
        trackEvent("bookScanFoundCanceled", createTrackEventData(new Pair<String, Object>("found_book_cancelled", 1)));
    }

    @Override
    public void trackOnBookShared() {
        trackEvent("shareBook", createTrackEventData(new Pair<String, Object>("share_book", 1)));
    }

    @Override
    public void trackOnBackupMade() {
        trackEvent("backupMade", createTrackEventData(new Pair<String, Object>("backupMade", 1)));
    }

    @Override
    public void trackOnBackupRestored() {
        trackEvent("backupRestored", createTrackEventData(new Pair<String, Object>("backupRestored", 1)));
    }

    @Override
    public void trackOnBookScanned(Book b) {
        Map<String, Object> data = createTrackEventData(
                new Pair<String, Object>("author", b.getAuthor()),
                new Pair<String, Object>("language", b.getLanguage()),
                new Pair<String, Object>("pages", b.getPageCount()));
        trackEvent("bookScanned", data);
    }

    @Override
    public void trackOnBookMovedToDone(Book b) {
        long duration = b.getEndDate() - b.getStartDate();
        trackEvent("bookFinished", createTrackEventData(new Pair<String, Object>("duration", duration)));
    }

    // -------------------------- Helper methods --------------------------

    private void initializeKeen(Context context) {

        // First initialize client
        KeenClient client = new AndroidKeenClientBuilder(context).build();
        KeenClient.initialize(client);

        // Then initialize project
        KeenProject project = new KeenProject(PROJECT_ID, WRITE_KEY, READ_KEY);
        KeenClient.client().setDefaultProject(project);
    }

    private Map<String, Object> createTrackEventData(@NonNull Pair<String, Object>... entries) {

        Map<String, Object> data = new HashMap<>();
        for (Pair<String, Object> p : entries) {
            data.put(p.first, p.second);
        }
        return data;
    }

    private void trackEvent(String event, Map<String, Object> data) {
        KeenClient.client().addEventAsync(event, data);
    }


}
