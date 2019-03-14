package com.liqingyue.imageclip.core

import android.graphics.*
import android.util.Log
import com.liqingyue.imageclip.core.clip.ClipImageWindowView

/**
 * Created By Liqingyue
 * 2019.3.6
 */
class ZoomImageView {

    private val TAG = "ZoomImageView"

    private var M: Matrix = Matrix()

    var rotation = 0f

    var mClipImageWindowView: ClipImageWindowView? = null

    private var frame: RectF = RectF()


    /**
     * 原始限制区域
     */
    var originFrame: RectF = RectF()

    private var mBitmap: Bitmap


    private var viewWidth = 0f
    private var viewHeight = 0f
    private var viewRatio = 0f

    private var centerX = 0f
    private var centerY = 0f

    private val mMatrix = Matrix()

    constructor(width: Float, height: Float, mBitmap: Bitmap, flag: Boolean) {
        this.mBitmap = mBitmap
        viewHeight = height
        viewWidth = width
        viewRatio = viewWidth / viewHeight
        centerX = width / 2
        centerY = height / 2
        if (flag) {
            frame.set(0f, (height - mBitmap.height.toFloat()) / 2,
                    mBitmap.width.toFloat(), mBitmap.height.toFloat() + (height - mBitmap.height.toFloat()) / 2)
            originFrame.set(0f, (height - mBitmap.height.toFloat()) / 2,
                    mBitmap.width.toFloat(), mBitmap.height.toFloat() + (height - mBitmap.height.toFloat()) / 2)
        } else {
            frame.set((width - mBitmap.width.toFloat()) / 2, 0f,
                    mBitmap.width.toFloat() + (width - mBitmap.width.toFloat()) / 2, mBitmap.height.toFloat())
            originFrame.set((width - mBitmap.width.toFloat()) / 2, 0f,
                    mBitmap.width.toFloat() + (width - mBitmap.width.toFloat()) / 2, mBitmap.height.toFloat())
        }
    }

    fun drawImage(canvas: Canvas?) {
//        mMatrix.setRotate(-done, centerX, centerY)
//        mMatrix.mapRect(frame)


        canvas?.save()
        canvas?.rotate(rotation, centerX, centerY)
        canvas?.drawBitmap(mBitmap, null, frame, null)
        canvas?.restore()


//        mMatrix.setRotate(done, centerX, centerY)
//        mMatrix.mapRect(frame)
        Log.d(TAG, "$frame")
    }

    fun drawLastImage(canvas: Canvas) {
        canvas.clipRect(mClipImageWindowView?.mFrame ?: frame)
        canvas.drawBitmap(mBitmap, null, frame, null)
    }


    fun onScroll(disX: Float, disY: Float) {
        M.setTranslate(-disX, -disY)
        M.mapRect(frame)

    }

    fun onScale(factor: Float, focusX: Float, focusY: Float) {
        if (factor == 1f) {
            return
        }
        M.setScale(factor, factor, focusX, focusY)
        M.mapRect(frame)
    }

    fun getScaleEnd(scale: Float, focusX: Float, focusY: Float): RectF {
        val tempFrame = RectF()
        tempFrame.set(frame)
        val mMatrix = Matrix()
        mMatrix.setScale(scale, scale, focusX, focusY)
        if (focusX > originFrame.centerX()) {
            mMatrix.postTranslate(-(focusX - originFrame.centerX()), 0f)
        } else {
            mMatrix.postTranslate(originFrame.centerX() - focusX, 0f)
        }
        if (focusY > originFrame.centerY()) {
            mMatrix.postTranslate(0f, -(focusY - originFrame.centerY()))
        } else {
            mMatrix.postTranslate(0f, originFrame.centerY() - focusY)
        }
        mMatrix.mapRect(tempFrame)
        return tempFrame
    }

    fun getResetTransEnd(): RectF {
        if (frame.left > mClipImageWindowView?.mFrame?.left!!) {
            onScroll(-mClipImageWindowView?.mFrame?.left!! + frame.left, 0f)
        }
        if (frame.top > mClipImageWindowView?.mFrame?.top!!) {
            onScroll(0f, -mClipImageWindowView?.mFrame?.top!! + frame.top)
        }
        if (frame.right < mClipImageWindowView?.mFrame?.right!!) {
            onScroll(frame.right - mClipImageWindowView?.mFrame?.right!!, 0f)
        }
        if (frame.bottom < mClipImageWindowView?.mFrame?.bottom!!) {
            onScroll(0f, frame.bottom - mClipImageWindowView?.mFrame?.bottom!!)
        }
        return frame
    }

    fun rotate() {
        rotation = (rotation + 90f) % 360
        val clipWindowWidth = mClipImageWindowView?.mFrame?.width() ?: 1f
        val clipWindowHeight = mClipImageWindowView?.mFrame?.height() ?: 1f
        val clipWindowRatio = clipWindowHeight / clipWindowWidth

        var scale = 1f
        if (clipWindowRatio < viewRatio) {
            scale = viewHeight / clipWindowWidth
        } else {
            scale = viewWidth / clipWindowHeight

        }
        M.setScale(scale, scale, centerX, centerY)
        M.setRotate(rotation, centerX, centerY)
        M.mapRect(frame)
        mClipImageWindowView?.rotate(scale)
    }


    fun getFrame(): RectF {
        return frame
    }

    fun setFrame(frame: RectF) {
        this.frame.set(frame)
    }

    fun getScale(): Float {
        return frame.height() / mBitmap.height
    }

    fun resetRotation() {
        rotation = 0f
    }

    fun getmBitmap(): Bitmap {
        return mBitmap
    }

}