package at.shockbytes.dante.util.tracking;

import at.shockbytes.dante.util.books.Book;

/**
 * @author Martin Macheiner
 *         Date: 01.06.2017.
 */

public interface Tracker {

    void trackOnScanBook();

    void trackOnScanBookCanceled();

    void trackOnBookManuallyEntered();

    void trackOnFoundBookCanceled();

    void trackOnBookShared();

    void trackOnBackupMade();

    void trackOnBackupRestored();

    void trackOnBookScanned(Book b);

    void trackOnBookMovedToDone(Book b);

}
