package at.shockbytes.dante.util.books;

import io.realm.RealmObject;

/**
 * @author Martin Macheiner
 *         Date: 28.08.2016.
 */
public class BookConfig extends RealmObject {

    private long lastPrimaryKey;

    public BookConfig() {
        this(0);
    }

    public BookConfig(long lastPrimaryKey) {
        this.lastPrimaryKey = lastPrimaryKey;
    }

    public long getLastPrimaryKey() {
        long key = lastPrimaryKey;
        lastPrimaryKey ++;
        return key;
    }

}
