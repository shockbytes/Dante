/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.shockbytes.dante.ui.custom.barcode.camera

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.RequiresPermission
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.Toast
import at.shockbytes.dante.R
import com.google.android.gms.vision.CameraSource

class CameraSourcePreview(private val cxt: Context, attrs: AttributeSet) : ViewGroup(cxt, attrs) {

    private val surfaceView: SurfaceView
    private var startRequested: Boolean = false
    private var isSurfaceAvailable: Boolean = false
    private var cameraSource: CameraSource? = null

    private var overlay: GraphicOverlay<*>? = null

    private val isPortraitMode: Boolean
        get() {
            return cxt.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        }

    init {
        startRequested = false
        isSurfaceAvailable = false

        surfaceView = SurfaceView(cxt)
        surfaceView.holder.addCallback(SurfaceCallback())
        addView(surfaceView)
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @Throws(Exception::class, SecurityException::class)
    fun start(cameraSource: CameraSource?) {
        if (cameraSource == null) {
            stop()
        }

        this.cameraSource = cameraSource

        if (this.cameraSource != null) {
            startRequested = true
            startIfReady()
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @Throws(Exception::class, SecurityException::class)
    fun start(cameraSource: CameraSource, overlay: GraphicOverlay<*>) {
        this.overlay = overlay
        start(cameraSource)
    }

    fun stop() {
        cameraSource?.stop()
    }

    fun release() {
        cameraSource?.release()
        cameraSource = null
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @Throws(Exception::class, SecurityException::class)
    private fun startIfReady() {
        if (startRequested && isSurfaceAvailable) {
            cameraSource?.start(surfaceView.holder)
            if (overlay != null) {
                val size = cameraSource?.previewSize
                val min = Math.min(size?.width ?: 0, size?.height ?: 0)
                val max = Math.max(size?.width ?: 0, size?.height ?: 0)
                if (isPortraitMode) {
                    // Swap width and height sizes when in portrait, rotate by 90 degrees
                    overlay?.setCameraInfo(min, max, cameraSource?.cameraFacing ?: 0)
                } else {
                    overlay?.setCameraInfo(max, min, cameraSource?.cameraFacing ?: 0)
                }
                overlay?.clear()
            }
            startRequested = false
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {

        override fun surfaceCreated(surface: SurfaceHolder) {
            isSurfaceAvailable = true
            try {
                startIfReady()
            } catch (e: Exception) {
                e.printStackTrace()
                checkForRuntimeException(e)
            } catch (se: SecurityException) {
                se.printStackTrace()
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            isSurfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

        var width = 320
        var height = 240
        val size = cameraSource?.previewSize
        if (size != null) {
            width = size.width
            height = size.height
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode) {
            val tmp = width

            width = height
            height = tmp
        }

        val layoutWidth = right - left
        val layoutHeight = bottom - top

        // Computes height and width for potentially doing fit width.
        var childWidth = layoutWidth
        var childHeight = (layoutWidth.toFloat() / width.toFloat() * height).toInt()

        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight
            childWidth = (layoutHeight.toFloat() / height.toFloat() * width).toInt()
        }

        for (i in 0 until childCount) {
            getChildAt(i).layout(0, 0, childWidth, childHeight)
        }

        try {
            startIfReady()
        } catch (e: Exception) {
            e.printStackTrace()
            checkForRuntimeException(e)
        } catch (se: SecurityException) {
            se.printStackTrace()
        }
    }

    private fun checkForRuntimeException(e: Exception) {
        if (e is RuntimeException) {
            Toast.makeText(context, R.string.preview_failed, Toast.LENGTH_LONG).show()
        }
    }
}
