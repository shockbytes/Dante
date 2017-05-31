package at.shockbytes.dante.util.backup;

/**
 * @author Martin Macheiner
 *         Date: 30.04.2017.
 */

public class BackupEntry {

    private String fileId;
    private String fileName;
    private String device;
    private String storageProvider;

    private int books;
    private long timestamp;
    private boolean isAutoBackup;

    public BackupEntry(String fileId, String fileName, String device, String storageProvider,
                       int books, long timestamp, boolean isAutoBackup) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.device = device;
        this.storageProvider = storageProvider;
        this.books = books;
        this.timestamp = timestamp;
        this.isAutoBackup = isAutoBackup;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getStorageProvider() {
        return storageProvider;
    }

    public void setStorageProvider(String storageProvider) {
        this.storageProvider = storageProvider;
    }

    public int getBooks() {
        return books;
    }

    public void setBooks(int books) {
        this.books = books;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAutoBackup() {
        return isAutoBackup;
    }

    public void setAutoBackup(boolean autoBackup) {
        isAutoBackup = autoBackup;
    }
}
