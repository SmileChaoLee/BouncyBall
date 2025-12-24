package com.smile.bouncyball.models

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.smile.bouncyball.tools.LogUtil
import kotlin.math.cos
import kotlin.math.sin

class Triangle(
    // Origin Coordinates (0, 0) is on on the top - left
    // (x, y) is the center of the triangle
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f,
    val mColor: Int = 0) {

    companion object {
        private const val TAG = "Triangle"
    }

    var x = x
        set(value) {
            val dx = value - x
            threeP.topX += dx
            threeP.leftX += dx
            threeP.rightX += dx
            field = value
            setPath()
        }

    var y = y
        set(value) {
            val dy = value - y
            threeP.topY += dy
            threeP.leftY += dy
            threeP.rightY += dy
            field = value
            setPath()
        }

    var width = width
        set(value) {
            if (value < 0) return
            // keep the coordinates of the center unchanged
            if (field == 0f) {
                threeP.leftX = x - value / 2f
                threeP.leftY = y + height / 2f
                threeP.rightX = x + value / 2f
                threeP.rightY = y + height / 2f
            } else {
                val ratio = ((field - value) / 2f) / field
                var vector1 = threeP.rightX - threeP.leftX
                var vector2 = threeP.rightY - threeP.leftY
                vector1 = vector1 * ratio
                vector2 = vector2 * ratio
                threeP.leftX += vector1
                threeP.leftY -= vector2
                threeP.rightX -= vector1
                threeP.rightY += vector2
            }
            field = value
            setPath()
        }

    var height = height
        set(value) {
            if (value < 0) return
            // keep the coordinates of the center unchanged
            if (field == 0f) {
                threeP.topX = x
                threeP.topY = y - value / 2f
                threeP.leftY = y + value / 2f
                threeP.rightY = y + value / 2f
            } else {
                val ratio = ((field - value) / 2f) / width
                val midX = (threeP.leftX + threeP.rightX) / 2f
                val midY = (threeP.leftY + threeP.rightY) / 2f
                var vector1 = midX - threeP.topX
                var vector2 = midY - threeP.topY
                vector1 = vector1 * ratio
                vector2 = vector2 * ratio
                threeP.topX -= vector1
                threeP.topY += vector2
                threeP.leftX -= vector1
                threeP.leftY -= vector2
                threeP.rightX += vector1
                threeP.rightY -= vector2
            }
            field = value
            setPath()
        }

    var threeP = ThreePoints()
        set(value) {
            field = value
            setPath()
        }
    private val path = Path()
    private val paint = Paint().apply {
        color = mColor
        style = Paint.Style.FILL_AND_STROKE // or Paint.Style.STROKE for an outline
        isAntiAlias = true
    }

    init {
        threeP.topX = x
        threeP.topY = y - height / 2f
        threeP.leftX = x - width / 2f
        threeP.leftY = y + height / 2f
        threeP.rightX = x + width / 2f
        threeP.rightY = y + height / 2f
        setPath()
    }

    private fun setPath() {
        // Build the path
        LogUtil.d(TAG, "setPath.top = ${threeP.topX}, topY = ${threeP.topY}")
        LogUtil.d(TAG, "setPath.left = ${threeP.leftX}, leftY = ${threeP.leftY}")
        LogUtil.d(TAG, "setPath.right = ${threeP.rightX}, rightY = ${threeP.rightY}")
        LogUtil.d(TAG, "setPath.size = $width, height = $height")
        path.reset()
        path.moveTo(threeP.topX, threeP.topY)
        path.lineTo(threeP.leftX, threeP.leftY)
        path.lineTo(threeP.rightX, threeP.rightY)
        path.close() // Connects the last point back to the first
    }

    fun setWidthOld(width2: Float) {
        LogUtil.d(TAG, "setWidth.width2 = $width2")
        if (width2 < 0) return
        // keep the coordinates of the center unchanged
        if (width == 0f) {
            threeP.leftX = x - width2 / 2f
            threeP.leftY = y + height / 2f
            threeP.rightX = x + width2 / 2f
            threeP.rightY = y + height / 2f
        } else {
            val ratio = ((width - width2) / 2f) / width
            var vector1 = threeP.rightX - threeP.leftX
            LogUtil.d(TAG, "vector1 = $vector1")
            var vector2 = threeP.rightY - threeP.leftY
            LogUtil.d(TAG, "vector2 = $vector2")
            LogUtil.d(TAG, "ratio = $ratio")
            vector1 = vector1 * ratio
            LogUtil.d(TAG, "vector1 = $vector1")
            vector2 = vector2 * ratio
            LogUtil.d(TAG, "vector2 = $vector2")
            threeP.leftX += vector1
            threeP.leftY -= vector2
            threeP.rightX -= vector1
            threeP.rightY += vector2
        }
        this.width = width2
        setPath()
    }

    fun setHeightOld(height2: Float) {
        LogUtil.d(TAG, "setHeight.height2 = $height2")
        if (height2 < 0) return
        // keep the coordinates of the center unchanged
        if (height == 0f) {
            threeP.topX = x
            threeP.topY = y - height2 / 2f
            threeP.leftY = y + height2 / 2f
            threeP.rightY = y + height2 / 2f
        } else {
            val ratio = ((height - height2) / 2f) / width
            val midX = (threeP.leftX + threeP.rightX) / 2f
            val midY = (threeP.leftY + threeP.rightY) / 2f
            var vector1 = midX - threeP.topX
            var vector2 = midY - threeP.topY
            vector1 = vector1 * ratio
            vector2 = vector2 * ratio
            threeP.topX -= vector1
            threeP.topY += vector2
            threeP.leftX -= vector1
            threeP.leftY -= vector2
            threeP.rightX += vector1
            threeP.rightY -= vector2
        }
        this.height = height2
        setPath()
    }

    fun rotate(angle: Float, cx: Float = x, cy: Float = y) {
        LogUtil.d(TAG, "rotate")
        val tx = threeP.topX - cx
        val ty = threeP.topY - cy
        val lx = threeP.leftX - cx
        val ly = threeP.leftY - cy
        val rx = threeP.rightX - cx
        val ry = threeP.rightY - cy
        threeP.topX = cx + tx * cos(angle) - ty * sin(angle)
        threeP.topY = cy + tx * sin(angle) + ty * cos(angle)
        threeP.leftX = cx + lx * cos(angle) - ly * sin(angle)
        threeP.leftY = cy + lx * sin(angle) + ly * cos(angle)
        threeP.rightX = cx + rx * cos(angle) - ry * sin(angle)
        threeP.rightY = cy + rx * sin(angle) + ry * cos(angle)
        setPath()
    }

    fun drawTriangle(canvas: Canvas) {
        LogUtil.d(TAG, "drawTriangle.canvas = $canvas")
        // Draw the path on the canvas
        canvas.drawPath(path, paint)
    }
}