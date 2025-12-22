package com.smile.bouncyball.models

import android.graphics.Bitmap

/**
 * Created by Chao Lee on 2017-11-15.
 */
class Banner(
    //  the coordinate (x-axis) of the banner
    var bannerX: Int,
    //  the coordinate (y-axis) of the banner
    var bannerY: Int,
    val bannerWidth: Int,
    val bannerHeight: Int,
    val bitmap: Bitmap?)
