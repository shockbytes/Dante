package at.shockbytes.dante.camera.analyzer

import android.annotation.SuppressLint
import android.util.Size
import android.view.Surface.ROTATION_180
import android.view.Surface.ROTATION_270
import android.view.Surface.ROTATION_90
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import at.shockbytes.dante.camera.IsbnVisionBarcode
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BarcodeAnalyzer(private val rotationDegrees: Int) : ImageAnalysis.Analyzer {

    private val detector: FirebaseVisionBarcodeDetector

    private var lastAnalyzedTimestamp = 0L

    private val publisher = PublishSubject.create<IsbnVisionBarcode>()

    fun getBarcodeStream(): Observable<IsbnVisionBarcode> {
        return publisher.throttleFirst(1000, TimeUnit.MILLISECONDS)
    }

    init {

        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_EAN_13,
                FirebaseVisionBarcode.FORMAT_EAN_8
            )
            .build()

        detector = FirebaseVision
            .getInstance()
            .getVisionBarcodeDetector(options)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        val currentTimestamp = System.currentTimeMillis()

        val size = Size(imageProxy.width, imageProxy.height)

        val proxImage = imageProxy.image
        if (proxImage == null) {
            Timber.e("CAM: Image is null")
            return
        }

        val rotationCompensation = getRotation(rotationDegrees)
        val image = FirebaseVisionImage.fromMediaImage(proxImage, rotationCompensation)

        detector.detectInImage(image)
            .addOnSuccessListener { codes ->
                if (codes.isNotEmpty()) {

                    codes
                        .first { code ->
                            code.rawValue != null
                        }
                        ?.let { code ->
                            val isbnBarcode = IsbnVisionBarcode(
                                code.rawValue!!,
                                code.cornerPoints?.toList(),
                                code.boundingBox,
                                sourceSize = size,
                                sourceRotationDegrees = rotationDegrees
                            )

                            imageProxy.close()
                            publisher.onNext(isbnBarcode)
                        }
                }
                imageProxy.close()
            }
            .addOnFailureListener { exception ->
                Timber.e(exception)
                imageProxy.close()
            }

        lastAnalyzedTimestamp = currentTimestamp

        // Close proxy at the end
        imageProxy.close()
    }

    private fun getRotation(rotationCompensation: Int): Int {
        return when (rotationCompensation) {
            ROTATION_90 -> FirebaseVisionImageMetadata.ROTATION_90
            ROTATION_180 -> FirebaseVisionImageMetadata.ROTATION_180
            ROTATION_270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> FirebaseVisionImageMetadata.ROTATION_0
        }
    }
}