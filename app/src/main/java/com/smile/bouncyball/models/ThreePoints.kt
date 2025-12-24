package com.smile.bouncyball.models

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class ThreePoints(
    var topX:Float = 0f,
    var topY:Float = 0f,
    var leftX:Float = 0f,
    var leftY:Float = 0f,
    var rightX:Float = 0f,
    var rightY:Float = 0f) {
    fun rotate(angle: Float, centerX: Float = 0f, centerY: Float = 0f): ThreePoints {
        val temp = ThreePoints()
        val tx = topX - centerX
        val ty = topY - centerY
        val lx = leftX - centerX
        val ly = leftY - centerY
        val rx = rightX - centerX
        val ry = rightY - centerY

        val ttx = tx * cos(angle) - ty * sin(angle)
        val tty = tx * sin(angle) + ty * cos(angle)
        val llx = lx * cos(angle) - ly * sin(angle)
        val lly = lx * sin(angle) + ly * cos(angle)
        val rrx = rx * cos(angle) - ry * sin(angle)
        val rry = rx * sin(angle) + ry * cos(angle)

        temp.topX = ttx + centerX
        temp.topY = tty + centerY
        temp.leftX = llx + centerX
        temp.leftY = lly + centerY
        temp.rightX = rrx + centerX
        temp.rightY = rry + centerY

        return temp
    }
}