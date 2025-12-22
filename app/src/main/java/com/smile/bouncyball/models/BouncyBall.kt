package com.smile.bouncyball.models

import android.graphics.Bitmap

/**
 * Created by Chao Lee on 2017-11-15.
 */
class BouncyBall(
    //  coordinate (x-axis) of the ball
    var ballX: Int,
    //  coordinate (y-axis) of the ball
    var ballY: Int,
    var ballSize: Int,
    var ballSpan: Int,
    var bitmap: Bitmap?) {
    var ballRadius: Int
        private set
    var direction: Int = 0

    init {
        ballRadius = this.ballSize / 2
    }
}
