package at.shockbytes.dante.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import android.transition.Slide
import android.view.Gravity
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import at.shockbytes.dante.camera.analyzer.BarcodeAnalyzer
import at.shockbytes.dante.camera.overlay.BarcodeBoundsOverlay
import at.shockbytes.dante.core.sdkVersionOrAbove
import at.shockbytes.dante.util.addTo
import com.google.common.util.concurrent.ListenableFuture
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_camera.*
import timber.log.Timber
import java.util.concurrent.Executors

/**
 * Note: Instead of calling `startCamera()` on the main thread, we use `view_finder.post { ... }`
 * to make sure that `view_finder` has already been inflated into the view when `startCamera()` is called.
 */
class BarcodeCaptureActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var camera: Camera

    private lateinit var imagePreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    private val overlay = BarcodeBoundsOverlay()

    private val executor = Executors.newSingleThreadExecutor()

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.exitTransition = Slide(Gravity.BOTTOM)
        window.enterTransition = Slide(Gravity.BOTTOM)
        setContentView(R.layout.activity_camera)
        supportActionBar?.hide()

        setFullscreen()

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        checkPermissions()
    }

    private fun setFullscreen() {
        if (sdkVersionOrAbove(Build.VERSION_CODES.R)) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            preview_view.post {
                startCamera()
            }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCamera() {

        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val rotation = getPreviewRotation()

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(rotation)
                .setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val barcodeAnalyzer = BarcodeAnalyzer(rotation)
            barcodeAnalyzer.getBarcodeStream()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (isbn, _, _, _, _) ->

                    if (::camera.isInitialized) {
                        cameraProvider.unbindAll()
                    }

                    BarcodeScanResultBottomSheetDialogFragment.newInstance(isbn, askForAnotherScan = true)
                        .setOnCloseListener {
                            overlay.showBarcodeObject(null)
                            startCamera()
                        }
                        .show(supportFragmentManager, "show-bottom-sheet-with-book")
                }, { throwable ->
                    Timber.e(throwable)
                })
                .addTo(compositeDisposable)

            imageAnalysis.setAnalyzer(executor, barcodeAnalyzer)

            imagePreview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(preview_view.display.rotation)
                .build()

            cameraProvider.unbindAll()

            camera = cameraProvider.bindToLifecycle(this, cameraSelector, imagePreview, imageAnalysis)

            enableTapToFocus()
            imagePreview.setSurfaceProvider(preview_view.surfaceProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun getPreviewRotation(): Int {
        /*
         * The nullable calls will not be required when the
         * app uses ViewBinding instead of the synthetic extensions.
         */
        return preview_view?.display?.rotation ?: 0
    }

    // TODO Incorporate torch
    private fun toggleTorch() {

        if (::camera.isInitialized) {
            if (camera.cameraInfo.torchState.value == TorchState.ON) {
                camera.cameraControl.enableTorch(false)
            } else {
                camera.cameraControl.enableTorch(true)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableTapToFocus() {
        preview_view.setOnTouchListener { _, motionEvent ->

            val x = motionEvent.x
            val y = motionEvent.y

            val factory = preview_view.meteringPointFactory
            val point = factory.createPoint(x, y)
            val action = FocusMeteringAction.Builder(point).build()
            camera.cameraControl.startFocusAndMetering(action)

            true
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                preview_view.post {
                    startCamera()
                }
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all { permission ->
        ContextCompat.checkSelfPermission(
            baseContext,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10

        fun newIntent(context: Context): Intent {
            return Intent(context, BarcodeCaptureActivity::class.java)
        }
    }
}
