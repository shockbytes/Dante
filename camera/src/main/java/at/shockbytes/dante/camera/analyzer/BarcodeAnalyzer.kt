package at.shockbytes.dante.camera.analyzer

import android.app.Activity
import android.util.Size
import android.view.Surface.ROTATION_180
import android.view.Surface.ROTATION_270
import android.view.Surface.ROTATION_90
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import at.shockbytes.dante.camera.IsbnVisionBarcode
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

    override fun analyze(imageProxy: ImageProxy) {

        Timber.d("IMAGE: Analyze new frame")

        val currentTimestamp = System.currentTimeMillis()
        // Only search for ISBN with f=10
        // TODO Fix this
        if (currentTimestamp - lastAnalyzedTimestamp < TimeUnit.MILLISECONDS.toMillis(100)) {
            Timber.d("IMAGE: Are you kidding me?")
            // return
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

        val yb = y.buffer.remaining()
        val ub = u.buffer.remaining()
        val vb = v.buffer.remaining()
        val data = ByteArray(yb + ub + vb)

        y.buffer.get(data, 0, yb)
        u.buffer.get(data, yb, ub)
        v.buffer.get(data, yb + ub, vb)
        val image = FirebaseVisionImage.fromByteArray(data, metadata)

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