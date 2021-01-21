package at.shockbytes.dante.camera.analyzer

import android.annotation.SuppressLint
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import at.shockbytes.dante.camera.IsbnVisionBarcode
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BarcodeAnalyzer(private val rotationDegrees: Int) : ImageAnalysis.Analyzer {

    private val detector: BarcodeScanner

    private var lastAnalyzedTimestamp = 0L

    private val publisher = PublishSubject.create<IsbnVisionBarcode>()

    fun getBarcodeStream(): Observable<IsbnVisionBarcode> {
        return publisher.throttleFirst(1000, TimeUnit.MILLISECONDS)
    }

    init {

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8
            )
            .build()

        detector = BarcodeScanning.getClient(options)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        val currentTimestamp = System.currentTimeMillis()

        val size = Size(imageProxy.width, imageProxy.height)

        val proxyImage = imageProxy.image
        if (proxyImage == null) {
            Timber.e("CAM: Image is null")
            return
        }

        val image = InputImage.fromMediaImage(proxyImage, imageProxy.imageInfo.rotationDegrees)

        detector.process(image)
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
}