package com.liqingyue.imageclip.core

import android.animation.ValueAnimator
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator

class ClipImageAnimator() : ValueAnimator() {

    private var isRotate = false
    private var mEvaluator: ClipImageEvaluator? = null

    init {
        interpolator = AccelerateDecelerateInterpolator()
    }

    override fun setObjectValues(vararg values: Any?) {

        super.setObjectValues(*values)
        if (mEvaluator == null) {
            mEvaluator = ClipImageEvaluator()
        }
        setEvaluator(mEvaluator)
    }

    fun setChangeState(sState: ClipImageState, eState: ClipImageState) {
        setObjectValues(sState, eState)
        isRotate = ClipImageState.isRotate(sState, eState)
    }

    fun isRotate(): Boolean {
        return isRotate
    }
}