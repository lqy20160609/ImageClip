# ImageClip

一个小白第一次上传项目到github

一个图片裁剪库，使用方法也很简单

核心是一个ClipImageView

使用的时候只需要在布局文件中设置好宽高
随后调用



```
clipImageView?.post {
    clipImageView?.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.baidu))
}
```


同时方法
```
fun setBitmap(context: Context, imageUri: Uri)
```
可以通过uri设置图片

裁剪完成后可以通过方法

```
if (clipImageView?.isSteady == true) 
    val bitmap = clipImageView?.getClippedImage()
```
  
获得裁剪后的图片 但是请在之前进行isSteady的判断是否属于可裁剪状态
否则会出现图片区域发生错误。

旋转功能还在实现中。。。。


PS代码写的真的挺乱，上学党经验不足，时间不太够，抽时间再优化代码结构

具体实现和解析移步博客
https://blog.csdn.net/ruozhalqy

查看效果移步
http://www.iqiyi.com/w_19s6z4tq69.html
