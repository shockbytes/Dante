package at.shockbytes.dante.util.books;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import java.util.List;
import java.util.Map;

import at.shockbytes.dante.util.backup.BackupManager;
import rx.Observable;

/**
 * @author Martin Macheiner
 *         Date: 27.08.2016.
 */
public interface BookManager {

    Book addBook(@NonNull Book book);

    Book getBook(long id);

    void updateBookState(@NonNull Book book, Book.State newState);

    void updateCurrentBookPage(@NonNull Book book, int page);

    void updateBookStateAndPage(@NonNull Book book, Book.State state, int page);

    void removeBook(long id);

    Map<String, Integer> getStatistics();

    Observable<BookSuggestion> downloadBook(@NonNull String isbn);

    Observable<List<Book>> getAllBooks();

    Observable<Void> restoreBackup(FragmentActivity activity, List<Book> backupBooks,
                          BackupManager.RestoreStrategy strategy);

    List<Book> getAllBooksSync();

    Observable<List<Book>> getBookByState(Book.State state);

    void close();

}
