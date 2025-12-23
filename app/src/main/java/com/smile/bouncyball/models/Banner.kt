package com.smile.bouncyball.models

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

/**
 * Created by Chao Lee on 2017-11-15.
 */
class Banner(
    //  the coordinate (x-axis) of the banner
    var bannerX: Int = 0,
    //  the coordinate (y-axis) of the banner
    var bannerY: Int = 0,
    val bannerWidth: Int = 0,
    val bannerHeight: Int = 0,
    val bitmap: Bitmap? = null) {

    fun draw(canvas: Canvas, rect: Rect) {
        bitmap?.let { bm ->
            canvas.drawBitmap(bm, null, rect, null)
        }
    }
}
