package com.smile.bouncyball.models

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

class RectBitmap(
    left: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0,
    var bitmap: Bitmap? = null) {

    var left = left
        set(value) {
            field = value
            rect.left = value
        }

    var top = top
        set(value) {
            field = value
            rect.top = value
        }

    var right = right
        set(value) {
            field = value
            rect.right = value
        }

    var bottom = bottom
        set(value) {
            field = value
            rect.bottom = value
        }

    private val rect = Rect(left, top, right, bottom)

    fun set(left: Int, top: Int, right: Int, bottom: Int, bitmap: Bitmap? = null) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
        this.bitmap = bitmap
        rect.set(left, top, right, bottom)
    }

    fun contains(x: Int, y: Int): Boolean {
        return rect.contains(x, y)
    }

    fun draw(canvas: Canvas) {
        bitmap?.let {
            // rect.set(left, top, right, bottom)
            canvas.drawBitmap(it, null, rect, null)
        }
    }
}