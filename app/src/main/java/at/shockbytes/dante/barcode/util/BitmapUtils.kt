package at.shockbytes.dante.barcode.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import at.shockbytes.util.AppUtils
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/** Utils functions for bitmap conversions.  */
object BitmapUtils {

    // Convert NV21 format byte buffer to bitmap.
    fun getBitmap(data: ByteBuffer, metadata: FrameMetadata): Bitmap? {
        data.rewind()
        val imageInBuffer = ByteArray(data.limit())
        data.get(imageInBuffer, 0, imageInBuffer.size)
        try {
            val image = YuvImage(
                imageInBuffer, ImageFormat.NV21, metadata.width, metadata.height, null)
            val stream = ByteArrayOutputStream()
            image.compressToJpeg(Rect(0, 0, metadata.width, metadata.height), 80, stream)

            val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())

            stream.close()
            return rotateBitmap(bmp, metadata.rotation, metadata.cameraFacing)
        } catch (e: Exception) {
            Timber.e(e)
        }

        return null
    }

    // Rotates a bitmap if it is converted from a bytebuffer.
    private fun rotateBitmap(bitmap: Bitmap, rotation: Int, facing: Int): Bitmap {
        val matrix = Matrix()
        var rotationDegree = 0
        when (rotation) {
            FirebaseVisionImageMetadata.ROTATION_90 -> rotationDegree = 90
            FirebaseVisionImageMetadata.ROTATION_180 -> rotationDegree = 180
            FirebaseVisionImageMetadata.ROTATION_270 -> rotationDegree = 270
            else -> {
            }
        }

        // Rotate the image back to straight.}
        matrix.postRotate(rotationDegree.toFloat())
        return if (facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            // Mirror the image along X axis for front-facing camera image.
            matrix.postScale(-1.0f, 1.0f)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }

    fun createRoundedBitmapFromColor(
        context: Context,
        size: Int,
        @ColorInt color: Int
    ): RoundedBitmapDrawable {

        val image = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        image.eraseColor(color)

        // val c = Canvas(image)
        // c.drawBitmap(image, 0f, 0f, null)

        val rdb = RoundedBitmapDrawableFactory.create(context.resources, image)
        rdb.isCircular = true
        return rdb
    }
}