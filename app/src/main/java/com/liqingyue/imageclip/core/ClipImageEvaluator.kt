package com.liqingyue.imageclip.core

import android.animation.TypeEvaluator
import android.graphics.RectF
import android.util.Log

class ClipImageEvaluator : TypeEvaluator<ClipImageState> {

    private val TAG = "ClipImageEvaluator"

    private var clipImageState: ClipImageState? = null


    override fun evaluate(fraction: Float, startValue: ClipImageState?, endValue: ClipImageState?): ClipImageState {
        if (startValue != null && endValue != null) {
            var left = startValue.frame.left + fraction * (endValue.frame.left - startValue.frame.left)
            var top = startValue.frame.top + fraction * (endValue.frame.top - startValue.frame.top)
            var right = startValue.frame.right + fraction * (endValue.frame.right - startValue.frame.right)
            var bottom = startValue.frame.bottom + fraction * (endValue.frame.bottom - startValue.frame.bottom)
            if (clipImageState != null) {
                clipImageState?.frame = RectF(left, top, right, bottom)

            } else {
                clipImageState = ClipImageState(RectF(left, top, right, bottom))
            }
            if (startValue.winFrame != null) {
                Log.d(TAG, "${endValue?.winFrame}")
                left = startValue.winFrame?.left!! + fraction * (endValue.winFrame?.left!! - startValue.winFrame?.left!!)
                top = startValue.winFrame?.top!! + fraction * (endValue.winFrame?.top!! - startValue.winFrame?.top!!)
                right = startValue.winFrame?.right!! + fraction * (endValue.winFrame?.right!! - startValue.winFrame?.right!!)
                bottom = startValue.winFrame?.bottom!! + fraction * (endValue.winFrame?.bottom!! - startValue.winFrame?.bottom!!)

                clipImageState?.winFrame = RectF(left, top, right, bottom)
            } else {
                clipImageState?.winFrame = null
            }
        }

        return clipImageState ?: ClipImageState(RectF(0f, 0f, 0f, 0f))
    }
}