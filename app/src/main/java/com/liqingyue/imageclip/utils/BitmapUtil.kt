package com.liqingyue.imageclip.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import java.io.IOException

object BitmapUtil {
    fun readPictureDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL)
            degree = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return degree
    }

    fun getBitmapFromSDCard(path: String, mHeight: Int, mWidth: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        // 计算比例值
        options.inSampleSize = calculateInSampleSize(options, mHeight, mWidth)
        options.inJustDecodeBounds = false
        if (mHeight == 0 || mWidth == 0) {
            BitmapFactory.decodeFile(path)
        }
        return BitmapFactory.decodeFile(path, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, mHeight: Int, mWidth: Int): Int {
        // 原尺寸大小
        val yHeight = options.outHeight
        val yWidth = options.outWidth

        var inSampleSize = 1
        // 如果宽度大的话根据宽度固定大小缩放
        if (yWidth > yHeight && yWidth > mWidth) {
            inSampleSize = yWidth / mWidth
        } else if (yWidth < yHeight && yHeight > mHeight) {
            inSampleSize = yHeight / mHeight
        } else {
            inSampleSize = yWidth / mWidth
        }// 如果高度高的话根据宽度固定大小缩放
        if (inSampleSize <= 0) {
            inSampleSize = 1
        }

        return inSampleSize
    }

}