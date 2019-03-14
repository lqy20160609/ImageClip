package com.liqingyue.imageclip.core

import android.graphics.RectF

class ClipImageState {

    var rotation: Float = 0f

    var frame: RectF = RectF()

    var winFrame: RectF? = null

    constructor(frame: RectF, winFrame: RectF? = null) {
        this.frame.set(frame)
        if (winFrame != null) {
            this.winFrame = RectF()
            this.winFrame?.set(winFrame)
        }
    }


    companion object {
        fun isRotate(a: ClipImageState, b: ClipImageState): Boolean {
            return java.lang.Float.compare(a.rotation, b.rotation) != 0
        }
    }
}