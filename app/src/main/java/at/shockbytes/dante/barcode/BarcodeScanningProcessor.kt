package at.shockbytes.dante.barcode

import android.graphics.Bitmap
import at.shockbytes.dante.barcode.util.BarcodeGraphic
import at.shockbytes.dante.barcode.util.FrameMetadata
import at.shockbytes.dante.barcode.util.GraphicOverlay
import at.shockbytes.dante.barcode.util.CameraImageGraphic
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import timber.log.Timber
import java.io.IOException

class BarcodeScanningProcessor : VisionProcessorBase<List<FirebaseVisionBarcode>>() {

    private val detector: FirebaseVisionBarcodeDetector by lazy {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_EAN_13)
            .build()

        FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Timber.e(e, "Exception thrown while trying to close Barcode Detector")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionBarcode>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionBarcode>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()

        originalCameraImage?.let {
            val imageGraphic = CameraImageGraphic(graphicOverlay, it)
            graphicOverlay.add(imageGraphic)
        }

        results.forEach { fbBarcode ->
            val barcodeGraphic = BarcodeGraphic(graphicOverlay, fbBarcode)
            graphicOverlay.add(barcodeGraphic)
        }
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Timber.e(e, "Barcode detection failed")
    }
}
