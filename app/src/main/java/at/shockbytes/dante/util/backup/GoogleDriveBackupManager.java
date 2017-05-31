package at.shockbytes.dante.util.backup;

import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import at.shockbytes.dante.util.books.Book;
import at.shockbytes.dante.util.books.BookManager;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * @author Martin Macheiner
 *         Date: 30.04.2017.
 */

public class GoogleDriveBackupManager implements BackupManager,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String LAST_BACKUP = "google_drive_last_backup";
    private static final String AUTO_BACKUP_ENABLED = "google_drive_auto_backup_enabled";
    private static final String STORAGE_TYPE = "gdrive";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 0x8764;
    private static final String MIME_JSON = "application/json";

    private static final long MILLIS_TWO_WEEKS = 1209600000L;

    private Gson gson;
    private Context context;
    private FragmentActivity activity;
    private SharedPreferences preferences;
    private OnConnectionStatusListener onStatusListener;

    private GoogleApiClient apiClient;

    @Inject
    public GoogleDriveBackupManager(Context context, SharedPreferences preferences,
                                    @Named("backup_gson") Gson gson) {
        this.context = context;
        this.preferences = preferences;
        this.gson = gson;
    }

    @Override
    public void connect(@NonNull FragmentActivity activity,
                        @NonNull OnConnectionStatusListener onStatusListener) {
        this.activity = activity;
        this.onStatusListener = onStatusListener;

        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(context)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        apiClient.connect();
    }

    @Override
    public void disconnect() {

        if (apiClient != null) {
            apiClient.disconnect();
        }

        activity = null;
    }

    @Override
    public boolean isAutoBackupEnabled() {
        return preferences.getBoolean(AUTO_BACKUP_ENABLED, false);
    }

    @Override
    public void setAutoBackupEnabled(boolean autoBackupEnabled) {
        preferences.edit().putBoolean(AUTO_BACKUP_ENABLED, autoBackupEnabled).apply();
    }

    @Override
    public long getLastBackupTime() {
        return preferences.getLong(LAST_BACKUP, 0);
    }

    @Override
    public Observable<List<BackupEntry>> getBackupList() {

        return Observable.defer(new Func0<Observable<List<BackupEntry>>>() {
            @Override
            public Observable<List<BackupEntry>> call() {

                DriveApi.MetadataBufferResult result = Drive.DriveApi
                                                                .getAppFolder(apiClient)
                                                                .listChildren(apiClient).await();
                List<BackupEntry> entries = fromMetadataToBackupEntries(result);
                result.release();
                return Observable.just(entries);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Void> removeBackupEntry(final BackupEntry entry) {

        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                if (!deleteDriveFile(DriveId.decodeFromString(entry.getFileId()))) {
                    return Observable.error(new Throwable(
                            new BackupException("Cannot delete backup entry: " + entry.getFileName())));
                }
                return Observable.empty();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Void> removeAllBackupEntries() {

        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {

                DriveApi.MetadataBufferResult result = Drive.DriveApi
                        .getAppFolder(apiClient)
                        .listChildren(apiClient).await();

                if (result.getMetadataBuffer() != null) {
                    for (Metadata buffer : result.getMetadataBuffer()) {
                        // Throw an exception if even 1 file cannot be deleted
                        if (!deleteDriveFile(buffer.getDriveId())) {
                            return Observable.error(new Throwable(
                                    new BackupException("Cannot delete one of the backups")));
                        }
                    }
                }
                return Observable.empty();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Void> backup(final List<Book> books) {

        if (books.size() <= 0) {
            return Observable.error(new BackupException("No books to backup"));
        }

        // Must be outside the observable, because otherwise this will cause a RealmException
        final String content = gson.toJson(books);
        final String filename = createFilename(books.size());

        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {

                // Either auto backup is disabled, or auto backup enabled and last backup
                // was longer than two weeks ago
                long backupThreshold = System.currentTimeMillis() - MILLIS_TWO_WEEKS;
                if (!isAutoBackupEnabled() || getLastBackupTime() < backupThreshold) {
                    return createFile(filename, content);
                }
                return Observable.empty();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Void> restoreBackup(final FragmentActivity activity, final BackupEntry entry,
                                          final BookManager bookManager, final RestoreStrategy strategy) {

        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {

                List<Book> books = booksFromEntry(entry);
                return bookManager.restoreBackup(activity, books, strategy);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        onStatusListener.onConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {

            try {
                connectionResult.startResolutionForResult(activity, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {

            onStatusListener.onConnectionFailed();
            GoogleApiAvailability.getInstance()
                    .getErrorDialog(activity, connectionResult.getErrorCode(),
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
        }
    }

    private void setLastBackupTime(long millis) {
        preferences.edit().putLong(LAST_BACKUP, millis).apply();
    }

    private boolean deleteDriveFile(DriveId driveId) {
        return driveId.asDriveFile().delete(apiClient).await().isSuccess();
    }

    private List<BackupEntry> fromMetadataToBackupEntries(DriveApi.MetadataBufferResult result) {

        List<BackupEntry> entries = new ArrayList<>();

        if (result.getMetadataBuffer() != null) {
            for (Metadata buffer : result.getMetadataBuffer()) {

                String fileId = buffer.getDriveId().encodeToString();
                String fileName = buffer.getTitle();

                try {

                    String[] data = fileName.split("_");
                    String storageProvider = data[0];
                    String device = data[4].substring(0, data[4].lastIndexOf("."));
                    boolean isAutoBackup = data[1].equals("auto");
                    long timestamp = Long.parseLong(data[2]);
                    int books = Integer.parseInt(data[3]);

                    entries.add(new BackupEntry(fileId, fileName, device, storageProvider,
                            books, timestamp, isAutoBackup));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.wtf("Dante", "Cannot parse file: " + fileName);
                }
            }
        }
        return entries;
    }

    private String createFilename(int books) {

        long timestamp = System.currentTimeMillis();
        String type = isAutoBackupEnabled() ? "auto" : "man";

        return STORAGE_TYPE + "_" +
                type + "_" +
                timestamp + "_" +
                books + "_" +
                Build.MODEL + ".json";
    }

    private List<Book> booksFromEntry(BackupEntry entry) {

        DriveFile file = DriveId.decodeFromString(entry.getFileId()).asDriveFile();
        DriveApi.DriveContentsResult result = file.open(apiClient, DriveFile.MODE_READ_ONLY, null).await();

        DriveContents contents = result.getDriveContents();
        BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
        StringBuilder builder = new StringBuilder();

        try {

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            String contentsAsString = builder.toString();
            // Close contents
            result.getDriveContents().discard(apiClient);

            return gson.fromJson(contentsAsString, new TypeToken<ArrayList<Book>>(){}.getType());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return empty array instead of a null array
        return new ArrayList<>();
    }

    private Observable<Void> createFile(String filename, String content) {

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(filename)
                .setMimeType(MIME_JSON)
                .build();

        DriveApi.DriveContentsResult driveContents = Drive.DriveApi.newDriveContents(apiClient).await();

        OutputStream out = driveContents.getDriveContents().getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

        try {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
            return Observable.error(new Throwable(e.getLocalizedMessage()));
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //driveContents.getDriveContents().commit(apiClient, changeSet)
        DriveFolder.DriveFileResult driveFileResult = Drive.DriveApi.getAppFolder(apiClient)
                .createFile(apiClient, changeSet, driveContents.getDriveContents())
                .await();

        if (driveFileResult.getStatus().isSuccess()) {
            setLastBackupTime(System.currentTimeMillis());
            return Observable.empty();
        } else {
            return Observable.error(new Throwable(driveFileResult.getStatus().getStatusMessage()));
        }
    }

}
