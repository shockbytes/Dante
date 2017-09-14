package at.shockbytes.dante.util.books;

import java.util.List;

/**
 * @author Martin Macheiner
 *         Date: 11.09.2017.
 */

public class BookSuggestion {

    private Book mainSuggestion;

    private List<Book> otherSuggestions;

    public BookSuggestion(Book mainSuggestion, List<Book> otherSuggestions) {
        this.mainSuggestion = mainSuggestion;
        this.otherSuggestions = otherSuggestions;
    }

    public Book getMainSuggestion() {
        return mainSuggestion;
    }

    public List<Book> getOtherSuggestions() {
        return otherSuggestions;
    }

    public boolean hasSuggestions() {
        return mainSuggestion != null;
    }

}
