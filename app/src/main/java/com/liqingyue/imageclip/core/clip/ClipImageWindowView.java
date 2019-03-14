package com.liqingyue.imageclip.core.clip;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;


public class ClipImageWindowView implements ClipImageWindowInterface {

    /**
     * 裁剪框边界
     */
    public RectF mFrame = new RectF();

    public RectF originFrame = new RectF();

    /**
     * 裁剪框限制区域
     * 默认为图片初始化区域
     * 旋转之后会发生改变
     */
    public RectF mWinFrame = new RectF();

    private float[] mCorners = new float[32];

    private float[][] mBaseSizes = new float[2][4];

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Matrix M = new Matrix();

    private static final int COLOR_FRAME = Color.WHITE;

    private static final int COLOR_CORNER = Color.WHITE;


    {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    public ClipImageWindowView() {

    }

    // 绘制裁剪框
    public void onDraw(Canvas canvas) {

        canvas.save();
        float[] size = {mFrame.width(), mFrame.height()};
        for (int i = 0; i < mBaseSizes.length; i++) {
            for (int j = 0; j < mBaseSizes[i].length; j++) {
                mBaseSizes[i][j] = size[i] * CLIP_SIZE_RATIO[j];
            }
        }

        for (int i = 0; i < mCorners.length; i++) {
            mCorners[i] = mBaseSizes[i & 1][CLIP_CORNER_STRIDES >>> i & 1]
                    + CLIP_CORNER_SIZES[CLIP_CORNERS[i] & 3]
                    + CLIP_CORNER_STEPS[CLIP_CORNERS[i] >> 2];
        }

        mPaint.setColor(COLOR_FRAME);
        mPaint.setStrokeWidth(CLIP_THICKNESS_FRAME);
        canvas.drawRect(mFrame, mPaint);

        canvas.translate(mFrame.left, mFrame.top);
        mPaint.setColor(COLOR_CORNER);
        mPaint.setStrokeWidth(CLIP_THICKNESS_SEWING);
        canvas.drawLines(mCorners, mPaint);
        canvas.restore();
    }

    // 获得锚点，即是否触摸裁剪框 返回空即未选中裁剪框
    public Anchor getAnchor(float x, float y) {
        if (Anchor.isCohesionContains(mFrame, -CLIP_CORNER_SIZE, x, y)
                && !Anchor.isCohesionContains(mFrame, CLIP_CORNER_SIZE, x, y)) {
            int v = 0;
            float[] cohesion = Anchor.getFrame(mFrame, 0);
            float[] pos = {x, y};
            for (int i = 0; i < cohesion.length; i++) {
                if (Math.abs(cohesion[i] - pos[i >> 1]) < CLIP_CORNER_SIZE) {
                    v |= 1 << i;
                }
            }

            return Anchor.valueOf(v);
        }
        return null;
    }

    public void onScroll(Anchor anchor, float dx, float dy) {
        anchor.move(mWinFrame, mFrame, dx, dy);
    }

    /**
     * 获得裁剪框需要修正的尺寸和位置
     */
    public Object[] getEndState() {
        RectF tempFrame = new RectF();
        tempFrame.set(mFrame);
        float mFrameRatio = mFrame.width() / mFrame.height();
        float mWinRatio = mWinFrame.width() / mWinFrame.height();
        float scale = 0f;
        if (mFrameRatio < mWinRatio) {
            scale = mWinFrame.height() / mFrame.height();
        } else {
            scale = mWinFrame.width() / mFrame.width();
        }
        Matrix M = new Matrix();
        if (mFrame.centerX() > mWinFrame.centerX()) {
            M.postTranslate(-(mFrame.width() / 2 + mFrame.left - mWinFrame.centerX()), 0f);
        } else {
            M.postTranslate(mFrame.width() / 2 + mWinFrame.centerX() - mFrame.right, 0f);
        }
        if (mFrame.centerY() > mWinFrame.centerY()) {
            M.postTranslate(0f, -(mFrame.height() / 2 + mFrame.top - mWinFrame.centerY()));
        } else {
            M.postTranslate(0f, mFrame.height() / 2 + mWinFrame.centerY() - mFrame.bottom);
        }
        M.postScale(scale, scale, mWinFrame.centerX(), mWinFrame.centerY());
        M.mapRect(tempFrame);
        return new Object[]{scale, tempFrame};
    }

    public void rotate(float scale) {
        M.reset();
        M.postScale(scale, scale, mFrame.centerX(), mFrame.centerY());
        M.postRotate(90f, mFrame.centerX(), mFrame.centerY());
        M.mapRect(mFrame);
    }
}
