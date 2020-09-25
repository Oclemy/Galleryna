package info.camposha.galleryna

import android.graphics.PointF
import android.graphics.RectF
import android.widget.ImageView.ScaleType

class Info(
    rect: RectF?,
    img: RectF?,
    widget: RectF?,
    base: RectF?,
    screenCenter: PointF?,
    scale: Float,
    degrees: Float,
    scaleType: ScaleType
) {
    // 内部图片在整个手机界面的位置
    var mRect = RectF()

    // 控件在窗口的位置
    var mImgRect = RectF()
    var mWidgetRect = RectF()
    var mBaseRect = RectF()
    var mScreenCenter = PointF()
    var mScale: Float
    var mDegrees: Float
    var mScaleType: ScaleType

    init {
        mRect.set(rect)
        mImgRect.set(img)
        mWidgetRect.set(widget)
        mScale = scale
        mScaleType = scaleType
        mDegrees = degrees
        mBaseRect.set(base)
        mScreenCenter.set(screenCenter)
    }
}