package at.shockbytes.dante.ui.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.HapticFeedbackConstants
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.custom.barcode.BarcodeGraphic
import at.shockbytes.dante.ui.custom.barcode.BarcodeTrackerFactory
import at.shockbytes.dante.ui.custom.barcode.camera.CameraSourcePreview
import at.shockbytes.dante.ui.custom.barcode.camera.GraphicOverlay
import at.shockbytes.dante.ui.fragment.dialog.SimpleRequestDialogFragment
import at.shockbytes.dante.util.tracking.Tracker
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 01.01.2018.
 */
class QueryCaptureFragment : BaseFragment(),
        GraphicOverlay.OnGraphicAvailableListener<BarcodeGraphic> {

    interface QueryCaptureCallback {

        fun onCameraPermissionDenied()

        fun onQueryAvailable(query: String?)

        fun onScannerNotOperational()
    }

    private var callback: QueryCaptureCallback? = null

    private val rcHandleGms = 9001
    private val rcHandleCameraPermission = 2

    private var cameraSource: CameraSource? = null
    private var previewView: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay<BarcodeGraphic>? = null

    @Inject
    protected lateinit var tracker: Tracker

    @Inject
    protected lateinit var prefs: SharedPreferences

    override val layoutId = R.layout.fragment_query_capture

    override fun setupViews() {
        previewView = view?.findViewById(R.id.fragment_query_capture_preview)
        graphicOverlay = view?.findViewById(R.id.fragment_query_capture_graphic_overlay)
        graphicOverlay?.setOnGraphicAvailableListener(this)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        callback = context as? QueryCaptureCallback
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        showSnackbarInfo()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        previewView?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        previewView?.release()
    }

    override fun bindViewModel() {
        // Not needed...
    }

    override fun unbindViewModel() {
        // Not needed...
    }

    override fun onFirstGraphicAvailable(graphic: BarcodeGraphic) {
        graphic.barcode?.let { barcode ->
            graphicOverlay?.removeGraphicListener()
            sendResultToActivity(barcode.displayValue)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {

        if (requestCode != rcHandleCameraPermission) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createCameraSource()
            return
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    callback?.onCameraPermissionDenied()
                }
                .show()
    }

    @Throws(SecurityException::class)
    private fun startCameraSource() {

        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (code != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, code, rcHandleGms)
                    .show()
        }

        if (cameraSource != null) {
            try {
                previewView?.start(cameraSource!!, graphicOverlay!!)
            } catch (e: IOException) {
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    private fun checkPermissions() {

        val rc = ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource()
        } else {
            requestCameraPermission()
        }
    }

    private fun showSnackbarInfo() {

        if (prefs.getBoolean("show_scan_snackbar", true)) {
            showSnackbar(getString(R.string.help_scan), getString(R.string.dismiss), true) {
                prefs.edit().putBoolean("show_scan_snackbar", false).apply()
            }
        }
    }

    private fun requestCameraPermission() {

        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.CAMERA)) {
            requestPermissions(permissions, rcHandleCameraPermission)
        } else {
            SimpleRequestDialogFragment.newInstance(getString(R.string.camera_permission_title),
                    getString(R.string.permission_camera_rationale), R.drawable.ic_camera_lens)
                    .setOnAcceptListener {
                        requestPermissions(permissions, rcHandleCameraPermission)
                    }
                    .show(fragmentManager, "request-camera-permission-dialogfragment")
        }
    }

    private fun createCameraSource() {

        val barcodeDetector = BarcodeDetector.Builder(context).build()
        val barcodeFactory = BarcodeTrackerFactory(graphicOverlay!!)
        barcodeDetector.setProcessor(MultiProcessor.Builder(barcodeFactory).build())

        if (!barcodeDetector.isOperational) {
            callback?.onScannerNotOperational()
        }

        cameraSource = CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true)
                .setRequestedFps(30.0f)
                .build()
    }

    private fun sendResultToActivity(query: String?) {
        previewView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        callback?.onQueryAvailable(query)
    }

    companion object {

        fun newInstance(): QueryCaptureFragment {
            val fragment = QueryCaptureFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

    }

}