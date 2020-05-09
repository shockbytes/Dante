package at.shockbytes.dante.camera.overlay

import android.graphics.RectF
import android.util.Size
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_180
import android.view.Surface.ROTATION_270
import android.view.Surface.ROTATION_90
import timber.log.Timber
import kotlin.math.ceil
import kotlin.math.max

/**
 * Allows the tracked ArObject to be mapped to a other coordinate system, e.g. a view.
 *
 * We use this to map the coordinate system of the preview image we received from the camera API to the ArOverlayView's
 * coordinate system which will have an other size.
 */
class PositionTranslator(
    private val targetWidth: Int,
    private val targetHeight: Int
) {

    fun processObject(barcodeObject: BarcodeObject): BarcodeObject {

        // Rotate Size TODO Check this
        Timber.e("CAM: PROCESS ${barcodeObject.sourceRotationDegrees}")
        val rotatedSize =


            when (barcodeObject.sourceRotationDegrees) {
            ROTATION_90, ROTATION_270 -> barcodeObject.sourceSize // Size(barcodeObject.sourceSize.height, barcodeObject.sourceSize.width)
            ROTATION_0, ROTATION_180 -> Size(barcodeObject.sourceSize.height, barcodeObject.sourceSize.width) // barcodeObject.sourceSize
            else -> throw IllegalArgumentException("Unsupported rotation. Must be 0, 90, 180 or 270")
        }
        Timber.e("CAM: rotated: $rotatedSize")

        // Calculate scale
        val scaleX = rotatedSize.width.toDouble() / targetWidth
        val scaleY = rotatedSize.height.toDouble() / targetHeight
        val scale = max(scaleX, scaleY)
        val scaleF = scale.toFloat()
        val scaledSize = Size(ceil(rotatedSize.width * scale).toInt(), ceil(rotatedSize.height * scale).toInt())

        // Calculate offset (we need to center the overlay on the target)
        val offsetX = (targetWidth - scaledSize.width) / 2f
        val offsetY = (targetHeight - scaledSize.height) / 2f

        // Map bounding box
        val mappedBoundingBox = RectF().apply {
            left = barcodeObject.boundingBox.right * scaleF + offsetX
            top = barcodeObject.boundingBox.top * scaleF + offsetY
            right = barcodeObject.boundingBox.left * scaleF + offsetX
            bottom = barcodeObject.boundingBox.bottom * scaleF + offsetY
        }

        return barcodeObject.copy(
            boundingBox = mappedBoundingBox,
            sourceSize = Size(targetWidth, targetHeight)
        )
    }
}