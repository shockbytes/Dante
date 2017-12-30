package at.shockbytes.dante.util.backup;

import android.support.v4.app.FragmentActivity;

import java.util.List;

import at.shockbytes.dante.util.books.Book;
import at.shockbytes.dante.util.books.BookManager;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * @author Martin Macheiner
 *         Date: 30.04.2017.
 */

public interface BackupManager {

    interface OnConnectionStatusListener {

        void onConnectionFailed();

        void onConnected();
    }

    int RESOLVE_CONNECTION_REQUEST_CODE = 0x1231;

    enum RestoreStrategy {MERGE, OVERWRITE}

    void connect(FragmentActivity activity, OnConnectionStatusListener onStatusListener);

    void disconnect();

    boolean isAutoBackupEnabled();

    void setAutoBackupEnabled(boolean autoBackupEnabled);

    long getLastBackupTime();

    Observable<List<BackupEntry>> getBackupList();

    Completable removeBackupEntry(BackupEntry entry);

    Completable removeAllBackupEntries();

    Completable backup(List<Book> books);

    Completable restoreBackup(FragmentActivity activity, BackupEntry entry,
                                   BookManager bookManager, RestoreStrategy strategy);

}
