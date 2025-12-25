package com.smile.bouncyball.models

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect

/**
 * Created by Chao Lee on 2017-11-15.
 */
class BouncyBall(
    //  coordinate (x-axis) of the ball
    var ballX: Int = 0,
    //  coordinate (y-axis) of the ball
    var ballY: Int = 0,
    var ballSize: Int = 0,
    var bitmap: Bitmap? = null) {
    var ballRadius: Int
        private set
    var direction = 0
    var dirVector = Point(0, 0)
    var speed = 1.0f

    init {
        ballRadius = ballSize / 2
    }

    fun draw(canvas: Canvas, rect: Rect) {
        bitmap?.let { bm ->
            canvas.drawBitmap(bm, null, rect, null)
        }
    }
}
