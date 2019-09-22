package at.shockbytes.dante.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Rational
import android.util.Size
import android.view.MotionEvent
import android.view.Surface
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.core.graphics.toRectF
import at.shockbytes.dante.camera.overlay.BarcodeObject
import at.shockbytes.dante.camera.overlay.BarcodeBoundsOverlay
import at.shockbytes.dante.camera.overlay.PositionTranslator
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import androidx.camera.core.CameraX.LensFacing
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.camera.core.UseCase
import android.transition.Slide
import android.view.Gravity
import at.shockbytes.dante.camera.focus.FocusComputation
import at.shockbytes.dante.camera.preview.AutoFitPreviewBuilder
import at.shockbytes.dante.camera.analyzer.BarcodeAnalyzer
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_camera.*
import timber.log.Timber

/**
 * Note: Instead of calling `startCamera()` on the main thread, we use `view_finder.post { ... }`
 * to make sure that `view_finder` has already been inflated into the view when `startCamera()` is called.
 */
class BarcodeCaptureActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var useCases: Array<UseCase>
    private lateinit var preview: Preview

    private val overlay = BarcodeBoundsOverlay()
    private val barcodeAnalyzer = BarcodeAnalyzer()

    private val compositeDisposable = CompositeDisposable()
    private val focusComputation by lazy {
        FocusComputation(view_finder)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.exitTransition = Slide(Gravity.BOTTOM)
        window.enterTransition = Slide(Gravity.BOTTOM)
        setContentView(R.layout.activity_camera)
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        checkPermissions()
    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            view_finder.post {
                startCamera()
            }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCamera() {

        val metrics = DisplayMetrics().also { view_finder.display.getRealMetrics(it) }
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)

        preview = buildPreviewUseCase(screenAspectRatio)
        val analyzer = buildAnalyzerUseCase(screenAspectRatio)

        useCases = arrayOf(preview, analyzer)

        // Bind use cases to lifecycle
        CameraX.bindToLifecycle(this, *useCases)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            focusComputation.compute(event)
        }
        return false
    }

    private fun buildAnalyzerUseCase(screenAspectRatio: Rational): ImageAnalysis {

        // Setup image analysis pipeline that computes average pixel luminance
        val analyzerConfig = ImageAnalysisConfig.Builder()
            .apply {
                // Use a worker thread for image analysis to prevent glitches
                val analyzerThread = HandlerThread("BarcodeAnalysis").apply { start() }
                setCallbackHandler(Handler(analyzerThread.looper))
                // In our analysis, we care more about the latest image than analyzing *every* image
                setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                setTargetAspectRatio(screenAspectRatio)
            }
            .build()

        // Build the image analysis use case and instantiate our analyzer
        return ImageAnalysis(analyzerConfig).apply {
            analyzer = barcodeAnalyzer
        }
    }

    private fun buildPreviewUseCase(screenAspectRatio: Rational): Preview {

        val previewConfig = PreviewConfig.Builder()
            .apply {
                setTargetAspectRatio(screenAspectRatio)
                setLensFacing(LensFacing.BACK)
            }
            .build()

        return AutoFitPreviewBuilder.build(previewConfig, view_finder).apply {
            // Every time the viewfinder is updated, recompute layout
            setOnPreviewOutputUpdateListener { previewOutput ->

                // To update the SurfaceTexture, we have to remove it and re-add it
                (view_finder.parent as ViewGroup).apply {
                    removeView(view_finder)
                    addView(view_finder, 0)
                }

                view_finder.surfaceTexture = previewOutput.surfaceTexture
                updateTransform()
            }
        }
    }

    private fun addOverlayToViewFinder(
        isbn: String,
        bounds: Rect?,
        size: Size,
        rotationDegrees: Int
    ) {

        bounds?.let {
            PositionTranslator(overlay_view.width, overlay_view.height)
                .processObject(BarcodeObject(isbn, bounds.toRectF(), size, rotationDegrees)).run {
                    overlay.showBarcodeObject(this)
                }
        }
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = view_finder.width / 2f
        val centerY = view_finder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when (view_finder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        view_finder.setTransform(matrix)
    }

    override fun onResume() {
        super.onResume()

        barcodeAnalyzer.getBarcodeStream()
            .subscribe { (isbn, _, bounds, sourceSize, sourceRotationDegrees) ->
                focusComputation.rotationDegrees = sourceRotationDegrees

                addOverlayToViewFinder(isbn, bounds, sourceSize, sourceRotationDegrees)
                BarcodeScanResultBottomSheetDialogFragment.newInstance(isbn, askForAnotherScan = true)
                    .setOnCloseListener {
                        overlay.showBarcodeObject(null)
                        startCamera()
                    }
                    .show(supportFragmentManager, "show-bottom-sheet-with-book")
                CameraX.unbind(*useCases)
            }
            .addTo(compositeDisposable)

        focusComputation.onFocusEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (_, _) ->
                // TODO Enable later
                // view_finder.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                // preview.focus(focusRect, meteringRect)
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)

        // Every time the provided texture view changes, recompute layout
        view_finder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

        overlay_view.add(overlay)

        Handler().postDelayed({
            barcodeAnalyzer.triggerTest("Lord of the rings")
        }, 1500)
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
                view_finder.post {
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
