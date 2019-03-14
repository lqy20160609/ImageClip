package com.liqingyue.imageclip.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.Toast
import com.liqingyue.imageclip.core.ClipImageAnimator
import com.liqingyue.imageclip.core.ClipImageState
import com.liqingyue.imageclip.core.ZoomImageView
import com.liqingyue.imageclip.core.clip.ClipImageWindowInterface
import com.liqingyue.imageclip.core.clip.ClipImageWindowView
import com.liqingyue.imageclip.utils.BitmapUtil
import com.liqingyue.imageclip.utils.PathUtil
import kotlin.math.max


/**
 * Created By Liqingyue
 * 2019.3.6
 * 自定义view承载待裁剪的图片
 */

class ClipImageView : View, ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener,
        Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private val TAG = "ClipImageView"

    private lateinit var zoomImageView: ZoomImageView

    private lateinit var clipImageWindowView: ClipImageWindowView

    private var mAnchor: ClipImageWindowInterface.Anchor? = null

    private var isChanged = false

    // 是否正在缩放中
    private var isScaling = false

    /**
     * 是否可以获取裁剪后的图片
     * 务必在获得裁剪后的图片之前进行判断
     */
    var isSteady = true

    private var M: Matrix = Matrix()

    private var mGestureDetector: GestureDetector? = null

    private var mScaleGestureDetector: ScaleGestureDetector? = null

    private var scaleOffset = 1f


    /**
     * 缩放过大提示回调
     */
    public var scaleToLarge:()->Unit = {}

    /**
     * 在缩放过程中如果出现图片小于裁剪框
     * 松手后回到原来的状态
     * 避免图片小于裁剪框出现异常状态
     */
    private var needToReset = false

    private var mAnimator: ClipImageAnimator? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        mGestureDetector = GestureDetector(context, this)
        mScaleGestureDetector = ScaleGestureDetector(context, this)
        mAnimator = ClipImageAnimator()
        mAnimator?.duration = 300
        mAnimator?.addListener(this)
        mAnimator?.addUpdateListener(this)

    }

    private fun startAnimation(sState: ClipImageState, eState: ClipImageState) {
        if (mAnimator?.isRunning == true) {

        } else {
            mAnimator?.setChangeState(sState, eState)
            mAnimator?.start()
        }
    }

    /**
     * 直接通过bitmap对象设置图片
     */
    fun setBitmap(bitmap: Bitmap) {
        val realImageWidth: Float
        val realImageHeight: Float
        val realWidth = bitmap.width
        val realHeight = bitmap.height
        // 原图宽高比
        val realImageRatio = realWidth * 1f / realHeight

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        // true：左右顶边  false：上下顶边
        var flag = true

        if (viewWidth / viewHeight < realImageRatio) {
            realImageWidth = viewWidth
            flag = true
            realImageHeight = realImageWidth * 1f / realImageRatio
        } else {
            realImageHeight = viewHeight
            flag = false
            realImageWidth = realImageHeight * realImageRatio
        }

        val matrix = Matrix()

        matrix.postScale(realImageWidth / realWidth, realImageHeight / realHeight)

        val newBitmap = Bitmap.createBitmap(bitmap, 0, 0, realWidth, realHeight, matrix, true)

        zoomImageView = ZoomImageView(width.toFloat(), height.toFloat(), newBitmap, flag)
        this.clipImageWindowView = ClipImageWindowView()
        this.clipImageWindowView.mFrame = RectF(
                zoomImageView.getFrame().left + 10f,
                zoomImageView.getFrame().top + 10f,
                zoomImageView.getFrame().right - 10,
                zoomImageView.getFrame().bottom - 10)
        this.clipImageWindowView.originFrame.set(this.clipImageWindowView.mFrame)

        this.clipImageWindowView.mWinFrame.set(RectF(
                0 + 10f,
                0 + 10f,
                width - 10f,
                height - 10f))
        zoomImageView.mClipImageWindowView = this.clipImageWindowView
        invalidate()
    }

    /**
     * 通过Uri设置图片
     */
    fun setBitmap(context: Context, imageUri: Uri) {
        // 按照View的宽高对图片进行压缩取样，防止OOM
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        // 新图的宽高
        val newImageWidth: Float
        val newImageHeight: Float
        val absolutePath = PathUtil.getRealFilePath(context, imageUri)
        // 拍照得到的图片可能存在被旋转的情况，对角度进行修正
        val degree = BitmapUtil.readPictureDegree(absolutePath!!)
        val bitmapBounds = BitmapFactory.decodeFile(absolutePath, options)
        val originImageWidth = options.outWidth
        val originImageHeight = options.outHeight

        // 原图宽高比
        val originImageRatio = originImageWidth * 1f / originImageHeight


        // view宽高
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        // true：左右顶边  false：上下顶边
        var flag = true

        if (viewWidth / viewHeight < originImageRatio) {
            newImageWidth = viewWidth
            flag = true
            newImageHeight = newImageWidth * 1f / originImageRatio
        } else {
            newImageHeight = viewHeight
            flag = false
            newImageWidth = newImageHeight * originImageRatio
        }

        // 压缩过后的新图
        var newBitmap = BitmapUtil.getBitmapFromSDCard(absolutePath, newImageHeight.toInt(), newImageWidth.toInt())
        if (degree != 0) {
            newBitmap = rotateBitmap(degree.toFloat(), newBitmap)
        }
        zoomImageView = ZoomImageView(width.toFloat(), height.toFloat(), newBitmap, flag)
        this.clipImageWindowView = ClipImageWindowView()
        this.clipImageWindowView.mFrame = RectF(
                zoomImageView.getFrame().left + 10f,
                zoomImageView.getFrame().top + 10f,
                zoomImageView.getFrame().right - 10,
                zoomImageView.getFrame().bottom - 10)
        this.clipImageWindowView.originFrame.set(this.clipImageWindowView.mFrame)

        this.clipImageWindowView.mWinFrame.set(RectF(
                0 + 10f,
                0 + 10f,
                width - 10f,
                height - 10f))
        zoomImageView.mClipImageWindowView = this.clipImageWindowView
        invalidate()
    }

    private fun rotateBitmap(degree: Float, srcBitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.reset()
        matrix.setRotate(degree)
        return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.width, srcBitmap.height, matrix, true)
    }

    fun rotate() {
        zoomImageView.rotate()
        isChanged = true
        invalidate()
    }

    fun restore() {
        isChanged = false
        zoomImageView.resetRotation()
        startAnimation(ClipImageState(zoomImageView.getFrame(), clipImageWindowView.mFrame),
                ClipImageState(zoomImageView.originFrame, clipImageWindowView.originFrame))
    }

    /**
     * 获得裁剪后的图片
     */
    fun getClippedImage(): Bitmap? {

        if (!isChanged) {
            isChanged = false

            return zoomImageView.getmBitmap()
        }
        isChanged = false


        val scale = 1f / zoomImageView.getScale()

        val frame = RectF(clipImageWindowView.mFrame)


        val m = Matrix()
        m.setRotate(zoomImageView.rotation, frame.centerX(), frame.centerY())
        m.mapRect(frame)

        m.setScale(scale, scale, frame.left, frame.top)
        m.mapRect(frame)
        if (Math.round(frame.width())<=0||Math.round(frame.height())<=0) {
            scaleToLarge.invoke()
            return null
        }

        val bitmap = Bitmap.createBitmap(Math.round(frame.width()),
                Math.round(frame.height()), Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        canvas.translate(-frame.left, -frame.top)
        canvas.scale(scale, scale, frame.left, frame.top)

        onDrawClipImages(canvas)

        return bitmap
    }

    private fun onDrawClipImages(canvas: Canvas) {
        canvas.save()

        val clipFrame = clipImageWindowView.mFrame
        canvas.rotate(zoomImageView.rotation, clipFrame.centerX(), clipFrame.centerY())

        zoomImageView.drawLastImage(canvas)
    }


    override fun onDraw(canvas: Canvas?) {
        zoomImageView.drawImage(canvas)
        clipImageWindowView.onDraw(canvas)
    }

    /**
     * 缩放手势方法
     */
    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        isScaling = true
        Log.d(TAG, "onScaleBegin")
        needToReset = false
        isChanged = true
        return true
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        Log.d(TAG, "onScale")
        if (mAnchor != null) {
            return true
        }
        if (detector != null) {
            zoomImageView.onScale(detector.scaleFactor, scrollX + detector.focusX,
                    scrollY + detector.focusY)
            invalidate()
        }

        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        Log.d(TAG, "onScaleEnd")
        isScaling = false
        needToReset = (zoomImageView.getFrame().left > clipImageWindowView.mFrame.left + 1
                || zoomImageView.getFrame().top > clipImageWindowView.mFrame.top + 1
                || zoomImageView.getFrame().right < clipImageWindowView.mFrame.right - 1
                || zoomImageView.getFrame().bottom < clipImageWindowView.mFrame.bottom - 1)
    }

    /**
     * 其他手势方法
     */
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (mAnchor != null) {
            clipImageWindowView.onScroll(mAnchor, -distanceX, -distanceY)
            /**
             * 修正图片frame
             */
            if (zoomImageView.getFrame().left > clipImageWindowView.mFrame.left) {
                scaleOffset = zoomImageView.getFrame().width() / (zoomImageView.getFrame().width() - distanceX)
                zoomImageView.onScale(scaleOffset, clipImageWindowView.mFrame.centerX(),
                        clipImageWindowView.mFrame.centerY())
            }
            if (zoomImageView.getFrame().top > clipImageWindowView.mFrame.top) {
                scaleOffset = zoomImageView.getFrame().height() / (zoomImageView.getFrame().height() - distanceY)
                zoomImageView.onScale(scaleOffset, clipImageWindowView.mFrame.centerX(),
                        clipImageWindowView.mFrame.centerY())
            }
            if (zoomImageView.getFrame().right < clipImageWindowView.mFrame.right) {
                scaleOffset = zoomImageView.getFrame().width() / (zoomImageView.getFrame().width() + distanceX)
                zoomImageView.onScale(scaleOffset, clipImageWindowView.mFrame.centerX(),
                        clipImageWindowView.mFrame.centerY())
            }
            if (zoomImageView.getFrame().bottom < clipImageWindowView.mFrame.bottom) {
                scaleOffset = zoomImageView.getFrame().height() / (zoomImageView.getFrame().height() + distanceY)
                zoomImageView.onScale(scaleOffset, clipImageWindowView.mFrame.centerX(),
                        clipImageWindowView.mFrame.centerY())
            }
        } else {
            zoomImageView.onScroll(distanceX, distanceY)
        }
        invalidate()
        return true
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.d(TAG, "onDown")
        mAnchor = clipImageWindowView.getAnchor(e?.x!!, e.y)
        if (mAnchor != null) {
            isChanged = true
        }
        isSteady = false
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        Log.d(TAG, "onFling")
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.d(TAG, "onLongPress")
    }

    override fun onShowPress(e: MotionEvent?) {
        Log.d(TAG, "onShowPress")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.d(TAG, "onSingleTapUp")
        return true
    }


    /**
     * 动画更新相关
     */
    override fun onAnimationStart(animation: Animator?) {
        Log.d(TAG, "onAnimationStart")
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        val state = animation?.animatedValue as ClipImageState
        zoomImageView.setFrame(state.frame)
        if (state.winFrame != null) {
            clipImageWindowView.mFrame.set(state.winFrame)
        }
        invalidate()
    }

    override fun onAnimationEnd(animation: Animator?) {
        Log.d(TAG, "onAnimationEnd")
        mAnchor = null
        isSteady = true
    }

    override fun onAnimationRepeat(animation: Animator?) {
        Log.d(TAG, "onAnimationRepeat")
    }

    override fun onAnimationCancel(animation: Animator?) {
        Log.d(TAG, "onAnimationCancel")
        isSteady = true
    }

    /**
     * 监听手指抬起动作进行图片的复位
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        var handled = mScaleGestureDetector?.onTouchEvent(event) ?: false
        handled = mGestureDetector?.onTouchEvent(event) ?: false || handled

        if (event?.actionMasked == MotionEvent.ACTION_UP ||
                event?.actionMasked == MotionEvent.ACTION_CANCEL) {
            /**
             * 临时变量用于存储图片frame的起始状态作为动画的开始状态
             */
            val startFrame = RectF()
            startFrame.set(zoomImageView.getFrame())
            if (mAnchor != null) {
                val tempList = clipImageWindowView.endState
                val winEndState = tempList[1] as RectF
                val tempScale = tempList[0] as Float
                val imageEndState = zoomImageView.getScaleEnd(tempScale, clipImageWindowView.mFrame.centerX(),
                        clipImageWindowView.mFrame.centerY())
                // 如果裁剪框大于了图片的最终位置，则需要对图片新的位置进行修正
                M.reset()
                if (winEndState.left < imageEndState.left) {
                    M.postTranslate(winEndState.left - imageEndState.left, 0f)
                }
                if (winEndState.top < imageEndState.top) {
                    M.postTranslate(0f, winEndState.top - imageEndState.top)
                }
                if (winEndState.right > imageEndState.right) {
                    M.postTranslate(winEndState.right - imageEndState.right, 0f)
                }
                if (winEndState.bottom > imageEndState.bottom) {
                    M.postTranslate(0f, winEndState.bottom - imageEndState.bottom)
                }
                M.mapRect(imageEndState)
                startAnimation(ClipImageState(startFrame, clipImageWindowView.mFrame),
                        ClipImageState(imageEndState, winEndState))
                return handled
            }

            if (needToReset) {
                if (startFrame.width() < clipImageWindowView.mFrame.width() ||
                        startFrame.height() < clipImageWindowView.mFrame.height()) {
                    val scale = max(clipImageWindowView.mFrame.width() / startFrame.width(),
                            clipImageWindowView.mFrame.height() / startFrame.height())
                    zoomImageView.onScale(scale, clipImageWindowView.mFrame.centerX(),
                            clipImageWindowView.mFrame.centerY())
                }
                needToReset = false

            }
            startAnimation(ClipImageState(startFrame), ClipImageState(zoomImageView.getResetTransEnd()))
        }
        return handled
    }

    /**
     * 在view被移除的时候清空Animator防止内存泄露
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAnimator?.cancel()
        mAnimator?.removeAllListeners()
        mAnimator = null
    }

}