package com.liqingyue.imageclip

import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.liqingyue.imageclip.view.ClipImageView

/**
 * Created By Liqingyue
 * 2019.3.6
 */
class MainActivity : AppCompatActivity() {

    private var clipImageView: ClipImageView? = null
    var done: Button? = null
    var restore: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        done = findViewById(R.id.done)
        clipImageView = findViewById(R.id.Main_IMG)
        restore = findViewById(R.id.restore)
        /**
         * 一定确保view已经初始化完毕再设置图片，否则会因为宽高获取的不正确
         * 而导致crash
         */
        clipImageView?.post {
            clipImageView?.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.baidu))
            clipImageView?.scaleToLarge = {
                Toast.makeText(this, "图片放大倍数过大，请缩小后再尝试", Toast.LENGTH_SHORT).show()
            }
        }
        /**
         * 确保当前可以正确获得裁剪后的图片，通过isSteady属性判断
         */
        done?.setOnClickListener {
            if (clipImageView?.isSteady == true) {
                val bitmap = clipImageView?.getClippedImage() ?: return@setOnClickListener
                clipImageView?.setBitmap(bitmap)
            }
        }
        restore?.setOnClickListener {
            clipImageView?.restore()
        }

    }
}
