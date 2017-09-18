package at.shockbytes.dante.util.books;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import at.shockbytes.dante.network.BookDownloader;
import at.shockbytes.dante.util.AppParams;
import at.shockbytes.dante.util.backup.BackupException;
import at.shockbytes.dante.util.backup.BackupManager;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * @author Martin Macheiner
 *         Date: 27.08.2016.
 */
public class RealmBookManager implements BookManager {

    private Realm realm;

    private BookDownloader bookDownloader;

    @Inject
    public RealmBookManager(BookDownloader bookDownloader, Realm realm) {
        this.realm = realm;
        this.bookDownloader = bookDownloader;
    }

    @Override
    public Book addBook(@NonNull Book book) {

        realm.beginTransaction();

        long id = getLastId();
        book.setId(id);
        realm.copyToRealm(book);

        realm.commitTransaction();

        return book;
    }

    @Override
    public Book getBook(long id) {
        return realm.where(Book.class).equalTo("id", id).findFirst();
    }

    @Override
    public void updateBookState(@NonNull final Book book, final Book.State newState) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {

                book.setState(newState);
                realm.copyToRealmOrUpdate(book);
            }
        });

    }

    @Override
    public void updateCurrentBookPage(@NonNull final Book book, final int page) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {

                book.setCurrentPage(page);
                realm.copyToRealmOrUpdate(book);
            }
        });

    }

    @Override
    public void updateBookStateAndPage(@NonNull final Book book, final Book.State state,
                                       final int page) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {

                book.setCurrentPage(page);
                book.setState(state);
                realm.copyToRealmOrUpdate(book);
            }
        });
    }

    @Override
    public void removeBook(final long id) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                realm.where(Book.class).equalTo("id", id).findFirst().deleteFromRealm();
            }
        });
    }

    @Override
    public Map<String, Integer> getStatistics() {

        Map<String, Integer> stats = new HashMap<>();

        int upcoming = realm.where(Book.class)
                .equalTo("ordinalState", Book.State.READ_LATER.ordinal()).findAll().size();
        int current = realm.where(Book.class)
                .equalTo("ordinalState", Book.State.READING.ordinal()).findAll().size();
        List<Book> doneList = realm.where(Book.class)
                .equalTo("ordinalState", Book.State.READ.ordinal()).findAll();
        int done = doneList.size();
        int pages = 0;
        for (Book b : doneList) {
            pages += b.getPageCount();
        }
        stats.put(AppParams.STAT_UPCOMING, upcoming);
        stats.put(AppParams.STAT_CURRENT, current);
        stats.put(AppParams.STAT_DONE, done);
        stats.put(AppParams.STAT_PAGES, pages);
        return stats;
    }

    @Override
    public Observable<BookSuggestion> downloadBook(@NonNull String isbn) {
        return bookDownloader.downloadBookSuggestion(isbn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Book>> getAllBooks() {
        List<Book> books = realm.where(Book.class)
                .findAll()
                .sort("id", Sort.DESCENDING);
        return Observable.just(books)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Void> restoreBackup(FragmentActivity activity, List<Book> backupBooks,
                                          BackupManager.RestoreStrategy strategy) {

        switch (strategy) {

            case MERGE:
                return mergeBackupRestore(activity, backupBooks);

            case OVERWRITE:
                return overwriteBackupRestore(activity, backupBooks);

            default:
                return Observable.error(new BackupException("Cannot resolve restore strategy"));
        }
    }

    @Override
    public List<Book> getAllBooksSync() {
        return realm.where(Book.class)
                .findAll()
                .sort("id", Sort.DESCENDING);
    }

    @Override
    public Observable<List<Book>> getBookByState(Book.State state) {
        List<Book> books = realm.where(Book.class)
                .equalTo("ordinalState", state.ordinal())
                .findAll()
                .sort("id", Sort.DESCENDING);
        return Observable.just(books)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void close() {
        realm.close();
    }

    /**
     * This must always be called inside a transaction
     */
    private long getLastId() {

        // This is the only place where a real realm transaction is executed
        BookConfig config = realm.where(BookConfig.class).findFirst();
        if (config == null) { // First time config is written
            config = realm.createObject(BookConfig.class);
        }

        return config.getLastPrimaryKey();
    }

    private Observable<Void> mergeBackupRestore(final FragmentActivity activity,
                                                final List<Book> backupBooks) {

        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<Book> books = getAllBooksSync();
                        for (Book bBook : backupBooks) {

                            boolean insert = true;
                            for (Book sBook : books) {
                                if (sBook.getTitle().equals(bBook.getTitle())) {
                                    insert = false;
                                    break;
                                }
                            }

                            if (insert) {
                                addBook(bBook);
                            }
                        }
                    }
                });
                return Observable.empty();
            }
        });

    }

    private Observable<Void> overwriteBackupRestore(final FragmentActivity activity,
                                                    final List<Book> backupBooks) {

        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RealmResults<Book> stored = realm.where(Book.class).findAll();
                        realm.beginTransaction();
                        stored.deleteAllFromRealm();
                        realm.commitTransaction();

                        for (Book b : backupBooks) {
                            addBook(b);
                        }

                    }
                });


                return Observable.empty();
            }
        });



    }

}
