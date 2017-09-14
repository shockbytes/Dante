package at.shockbytes.dante.util.books;

import com.google.gson.JsonObject;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @author Martin Macheiner
 *         Date: 27.08.2016.
 */
public class Book extends RealmObject {

    public enum State {READ_LATER, READING, READ}

    @PrimaryKey
    private long id;

    /*
    * Switch the books in position in RecyclerView
    * To add it now will not break realm on a later update
    */
    private int position;

    private String title;
    private String subTitle;

    private String author;
    private int pageCount;
    private String publishedDate;
    private String isbn;

    private String thumbnailAddress;
    private String googleBooksLink;

    private int ordinalState;

    // Version 1
    private long startDate;
    private long endDate;
    private long wishlistDate;

    // Version 2
    private int rating; // 1 - 5
    private String language;

    // Version 3 TODO integrate
    private int currentPage;
    private String notes;

    public Book() {
        this("", "", "", 0, "", "", "", "", "");
    }

    public Book(String title, String subTitle, String author, int pageCount, String publishedDate,
                String isbn, String thumbnailAddress, String googleBooksLink, String language) {
        this(title, subTitle, author, pageCount, publishedDate, isbn,
                thumbnailAddress, googleBooksLink, 0, 0, 0, language, -1);
    }

    public Book(String title, String subTitle, String author, int pageCount, String publishedDate,
                String isbn, String thumbnailAddress, String googleBooksLink, long startDate,
                long endDate, long wishlistDate, String language, int rating) {
        this.id = -1;
        this.title = title;
        this.subTitle = subTitle;
        this.author = author;
        this.pageCount = pageCount;
        this.publishedDate = publishedDate;
        this.isbn = isbn;
        this.thumbnailAddress = thumbnailAddress;
        this.googleBooksLink = googleBooksLink;

        this.language = language;
        this.rating = rating;

        this.startDate = startDate;
        this.endDate = endDate;
        this.wishlistDate = wishlistDate;

        this.ordinalState = State.READ_LATER.ordinal();
    }


    public State getState() {
        return State.values()[ordinalState];
    }

    public void setState(State state) {

        switch (state) {

            case READ_LATER:
                wishlistDate = System.currentTimeMillis();
                startDate = 0;
                endDate = 0;
                break;
            case READING:
                startDate = System.currentTimeMillis();
                endDate = 0;
                break;
            case READ:
                endDate = System.currentTimeMillis();
                // If going from READ_LATER directly to READ state
                if (startDate == 0) {
                    startDate = endDate;
                }

                break;
        }

        this.ordinalState = state.ordinal();
    }


    public boolean isAnyTimeInformationAvailable() {
        return wishlistDate != 0 || startDate != 0 || endDate != 0;
    }

    public String getGoogleBooksLink() {
        return googleBooksLink;
    }

    public String getThumbnailAddress() {
        return thumbnailAddress;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating > 0 && rating <= 5) {
            this.rating = rating;
        }
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getWishlistDate() {
        return wishlistDate;
    }

    public void setWishlistDate(long wishlistDate) {
        this.wishlistDate = wishlistDate;
    }

    public String getAuthor() {
        return author;
    }

    public JsonObject toJson() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("position", getPosition());
        jsonObject.addProperty("title", getTitle());
        jsonObject.addProperty("subTitle", getSubTitle());
        jsonObject.addProperty("author", getAuthor());
        jsonObject.addProperty("pageCount", getPageCount());
        jsonObject.addProperty("publishedDate", getPublishedDate());
        jsonObject.addProperty("isbn", getIsbn());
        jsonObject.addProperty("language", getLanguage());
        jsonObject.addProperty("thumbnailAddress", getThumbnailAddress());
        jsonObject.addProperty("googleBooksLink", getGoogleBooksLink());
        jsonObject.addProperty("ordinalState", getState().ordinal());
        jsonObject.addProperty("rating", getRating());
        jsonObject.addProperty("startDate", getStartDate());
        jsonObject.addProperty("endDate", getEndDate());
        jsonObject.addProperty("wishlistDate", getWishlistDate());
        return jsonObject;
    }

    @Override
    public String toString() {

        String str = "\nId:\t" + id;
        str += "\nTitle:\t" + title;
        str += "\nSubtitle:\t" + subTitle;
        str += "\nAuthor:\t" + author;
        str += "\nPage count:\t" + pageCount;
        str += "\nPublished date:\t" + publishedDate;
        str += "\nISBN:\t" + isbn;
        str += "\nThumbnailaddress:\t" + thumbnailAddress;
        str += "\nGoogle books address:\t" + googleBooksLink;
        str += "\nStartdate:\t" + new Date(startDate).toString();
        str += "\nEnddate:\t" + new Date(endDate).toString();
        str += "\nWishlistdate:\t" + new Date(wishlistDate).toString();
        str += "\nLanguage:\t" + language;
        str += "\nRating:\t" + rating;
        str += "\nState:\t" + State.values()[ordinalState];
        return str;
    }
}
