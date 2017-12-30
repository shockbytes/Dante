/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.shockbytes.dante.util.barcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.inject.Inject;

import at.shockbytes.dante.R;
import at.shockbytes.dante.dagger.AppComponent;
import at.shockbytes.dante.ui.activity.BackNavigableActivity;
import at.shockbytes.dante.ui.activity.DownloadActivity;
import at.shockbytes.dante.ui.fragment.dialogs.QueryDialogFragment;
import at.shockbytes.dante.util.AppParams;
import at.shockbytes.dante.util.barcode.camera.CameraSourcePreview;
import at.shockbytes.dante.util.barcode.camera.GraphicOverlay;
import at.shockbytes.dante.util.tracking.Tracker;

/**
 * Activity for the multi-tracker app.  This app detects barcodes and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and ID of each barcode.
 */
public class QueryCaptureActivity extends BackNavigableActivity
        implements GraphicOverlay.OnGraphicAvailableListener<BarcodeGraphic>,
        QueryDialogFragment.OnQueryEnteredListener {

    private static final String TAG = "Dante";

    private static final int RC_HANDLE_GMS = 9001;
    private static final int REQ_CODE_DOWNLOAD_BOOK = 0x8434;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    
    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

    @Inject
    protected Tracker tracker;

    public static Intent newIntent(Context context) {
        return new Intent(context, QueryCaptureActivity.class);
    }

    @Override
    public void onCreate(Bundle icicle) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY); // Call has to happen before in order to avoid a crash
        super.onCreate(icicle);
        setContentView(R.layout.barcode_capture);
        setStatusBarTranslucent(true);
        setResult(RESULT_CANCELED, new Intent()); // Set this, otherwise this will trigger a Kotlin Exception

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(Color.parseColor(AppParams.TRANSLUCENT_ACTION_BAR_COLOR)));
        }

        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);
        mGraphicOverlay.setOnGraphicAvailableListener(this);

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.getBoolean("show_scan_snackbar", true)) {
            final Snackbar sb = Snackbar.make(mGraphicOverlay, R.string.help_scan, Snackbar.LENGTH_INDEFINITE);
            sb.setAction(R.string.dismiss, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sb.dismiss();
                    prefs.edit().putBoolean("show_scan_snackbar", false).apply();
                }
            });
            sb.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            sb.show();
        }
    }

    @Override
    public void injectToGraph(@NotNull AppComponent appComponent) {
        appComponent.inject(this);
    }

    private void setStatusBarTranslucent(boolean makeTranslucent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (makeTranslucent) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
    }

    private void requestCameraPermission() {

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_new_add_isbn) {
            QueryDialogFragment idf = QueryDialogFragment.newInstance();
            idf.setOnQueryEnteredListener(this);
            idf.show(getSupportFragmentManager(), "query-dialog-fragment");
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("InlinedApi")
    private void createCameraSource() {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector
                .Builder(context)
                .build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            /*
            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            } */
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        mCameraSource = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true)
                .setRequestedFps(30.0f)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQ_CODE_DOWNLOAD_BOOK:

                if (resultCode == RESULT_OK) {
                    long bookId = data.getLongExtra(AppParams.EXTRA_BOOK_ID, -1);
                    if (bookId > -1) {
                        Intent intentData = new Intent();
                        intentData.putExtra(AppParams.EXTRA_BOOK_ID, bookId);
                        setResult(RESULT_OK, data);
                    }
                }

                supportFinishAfterTransition();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
            return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(android.R.string.ok, listener)
                .show();
    }

    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void sendResultToCallingActivity(String query) {
        if (query != null) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
            startActivityForResult(DownloadActivity.newIntent(this, query),
                    REQ_CODE_DOWNLOAD_BOOK, options.toBundle());
        } else {
            Log.d(TAG, "Query data is null");
        }
    }

    @Override
    public void onFirstGraphicAvailable(BarcodeGraphic graphic) {
        final Barcode barcode = graphic.getBarcode();
        if (barcode != null) {
            mGraphicOverlay.removeGraphicListener();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendResultToCallingActivity(barcode.displayValue);
                }
            });
        }
    }

    @Override
    public void onQueryEntered(String query) {

        tracker.trackOnBookManuallyEntered();
        sendResultToCallingActivity(query);
    }

}
