package at.shockbytes.dante.util.backup;

import android.support.v4.app.FragmentActivity;

import java.util.List;

import at.shockbytes.dante.util.books.Book;
import at.shockbytes.dante.util.books.BookManager;
import rx.Observable;

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

    Observable<Void> removeBackupEntry(BackupEntry entry);

    Observable<Void> removeAllBackupEntries();

    Observable<Void> backup(List<Book> books);

    Observable<Void> restoreBackup(FragmentActivity activity, BackupEntry entry,
                                   BookManager bookManager, RestoreStrategy strategy);

}
