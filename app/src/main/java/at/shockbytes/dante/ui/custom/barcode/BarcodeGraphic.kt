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
package at.shockbytes.dante.ui.custom.barcode

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import at.shockbytes.dante.ui.custom.barcode.camera.GraphicOverlay
import com.google.android.gms.vision.barcode.Barcode

/**
 * Graphic instance for rendering barcode position, size, and ID within an associated graphic
 * overlay view.
 */
class BarcodeGraphic(overlay: GraphicOverlay<*>) : GraphicOverlay.Graphic(overlay) {

    var id: Int = 0

    private val selectedColor = Color.parseColor("#00DD99")

    private val mRectPaint: Paint = Paint()
    private val mTextPaint: Paint = Paint()

    @Volatile
    var barcode: Barcode? = null
        private set

    init {

        mRectPaint.color = selectedColor
        mRectPaint.style = Paint.Style.STROKE
        mRectPaint.strokeWidth = 6.0f

        mTextPaint.color = selectedColor
        mTextPaint.textSize = 36.0f
    }

    /**
     * Updates the barcode instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    fun updateItem(barcode: Barcode) {
        this.barcode = barcode
        postInvalidate()
    }

    /**
     * Draws the barcode annotations for position, size, and raw value on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        val barcode = this.barcode ?: return

        // Draws the bounding box around the barcode.
        val rect = RectF(barcode.boundingBox)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        // canvas.drawRect(rect, mRectPaint);

        val x = ((rect.right - rect.left) / 12).toInt()

        // Left line
        canvas.drawLine(rect.left, rect.top, rect.left, rect.bottom, mRectPaint)
        canvas.drawLine(rect.left, rect.top, rect.left + x, rect.top, mRectPaint)
        canvas.drawLine(rect.left, rect.bottom, rect.left + x, rect.bottom, mRectPaint)

        // Right line
        canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, mRectPaint)
        canvas.drawLine(rect.right, rect.top, rect.right - x, rect.top, mRectPaint)
        canvas.drawLine(rect.right, rect.bottom, rect.right - x, rect.bottom, mRectPaint)

        // Draws a label at the bottom of the barcode indicate the barcode value that was detected.
        canvas.drawText(barcode.rawValue, (rect.right - rect.left) / 2, rect.bottom, mTextPaint)
    }
}
