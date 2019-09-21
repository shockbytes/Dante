package at.shockbytes.camera.analyzer

import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import at.shockbytes.camera.IsbnVisionBarcode
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BarcodeAnalyzer : ImageAnalysis.Analyzer {

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

    override fun analyze(imageProxy: ImageProxy, rotationDegrees: Int) {

        val currentTimestamp = System.currentTimeMillis()
        // Only search for ISBN with f=10
        if (currentTimestamp - lastAnalyzedTimestamp < TimeUnit.MILLISECONDS.toMillis(100)) {
            return
        }

        val size = Size(imageProxy.width, imageProxy.height)

        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
            .setHeight(imageProxy.height)
            .setWidth(imageProxy.width)
            .setRotation(getRotation(rotationDegrees))
            .build()

        val y = imageProxy.planes[0]
        val u = imageProxy.planes[1]
        val v = imageProxy.planes[2]

        val Yb = y.buffer.remaining()
        val Ub = u.buffer.remaining()
        val Vb = v.buffer.remaining()
        val data = ByteArray(Yb + Ub + Vb)

        y.buffer.get(data, 0, Yb)
        u.buffer.get(data, Yb, Ub)
        v.buffer.get(data, Yb + Ub, Vb)
        val image = FirebaseVisionImage.fromByteArray(data, metadata)

        detector.detectInImage(image)
            .addOnSuccessListener { codes ->
                // Task completed successfully
                // ...
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

                            publisher.onNext(isbnBarcode)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "On failure ${exception.message}")
            }

        lastAnalyzedTimestamp = currentTimestamp
    }

    private fun getRotation(rotationCompensation: Int): Int {
        return when (rotationCompensation) {
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> FirebaseVisionImageMetadata.ROTATION_0
        }
    }
}