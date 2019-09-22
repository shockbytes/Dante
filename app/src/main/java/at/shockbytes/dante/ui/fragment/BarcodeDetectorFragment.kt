package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.barcode.BarcodeScanningProcessor
import at.shockbytes.dante.barcode.util.CameraSource
import at.shockbytes.dante.injection.AppComponent
import com.google.firebase.ml.common.FirebaseMLException
import kotlinx.android.synthetic.main.fragment_barcode_detector.*
import timber.log.Timber
import java.io.IOException

class BarcodeDetectorFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_barcode_detector

    private var cameraSource: CameraSource? = null

    override fun setupViews() {
        createCameraSource()
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        startCameraSource()
    }

    private fun startCameraSource() {
        cameraSource?.let {
            try {
                if (firePreview == null) {
                    Timber.d("resume: Preview is null")
                }
                if (fireFaceOverlay == null) {
                    Timber.d("resume: graphOverlay is null")
                }
                firePreview?.start(cameraSource!!, fireFaceOverlay)
            } catch (e: IOException) {
                Timber.e(e, "Unable to start camera source.")
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        firePreview?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
    }

    private fun createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(requireActivity(), fireFaceOverlay)
        }

        try {
            cameraSource?.setMachineLearningFrameProcessor(BarcodeScanningProcessor())
        } catch (e: FirebaseMLException) {
            Timber.e(e, "can not create camera source")
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance(): BarcodeDetectorFragment {
            return BarcodeDetectorFragment()
        }
    }
}