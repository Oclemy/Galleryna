package info.camposha.galleryna

import android.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.ImageView
import android.widget.OverScroller
import android.widget.Scroller
import info.camposha.galleryna.PhotoView.END

/**
 * Created by liuheng on 2015/6/21.
 *
 *
 * 如有任何意见和建议可邮件  bmme@vip.qq.com
 */
class PhotoView : androidx.appcompat.widget.AppCompatImageView {
    private var mMinRotate = 0
    /**
     * 获取动画持续时间
     */
    /**
     * 设置动画的持续时间
     */
    var animaDuring = 0
    /**
     * 获取最大可以缩放的倍数
     */
    /**
     * 设置最大可以缩放的倍数
     */
    var maxScale = 0f
    private var MAX_OVER_SCROLL = 0
    private var MAX_FLING_OVER_SCROLL = 0
    private var MAX_OVER_RESISTANCE = 0
    private var MAX_ANIM_FROM_WAITE = 500
    private val mBaseMatrix = Matrix()
    private val mAnimaMatrix = Matrix()
    private val mSynthesisMatrix = Matrix()
    private val mTmpMatrix = Matrix()
    private var mRotateDetector: RotateGestureDetector? = null
    private var mDetector: GestureDetector? = null
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mClickListener: OnClickListener? = null
    private var mScaleType: ScaleType? = null
    private var hasMultiTouch = false
    private var hasDrawable = false
    private var isKnowSize = false
    private var hasOverTranslate = false
    private var isEnable = false
    private var isRotateEnable = false
    private var isInit = false
    private var mAdjustViewBounds = false

    // 当前是否处于放大状态
    private var isZoonUp = false
    private var canRotate = false
    private var imgLargeWidth = false
    private var imgLargeHeight = false
    private var mRotateFlag = 0f
    private var mDegrees = 0f
    private var mScale = 1.0f
    private var mTranslateX = 0
    private var mTranslateY = 0
    private var mHalfBaseRectWidth = 0f
    private var mHalfBaseRectHeight = 0f
    private val mWidgetRect = RectF()
    private val mBaseRect = RectF()
    private val mImgRect = RectF()
    private val mTmpRect = RectF()
    private val mCommonRect = RectF()
    private val mScreenCenter = PointF()
    private val mScaleCenter = PointF()
    private val mRotateCenter = PointF()
    private val mTranslate =
        Transform()
    private var mClip: RectF? = null
    private var mFromInfo: Info? = null
    private var mInfoTime: Long = 0
    private var mCompleteCallBack: Runnable? = null
    private var mLongClick: OnLongClickListener? = null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        super.setScaleType(ScaleType.MATRIX)
        if (mScaleType == null) mScaleType = ScaleType.CENTER_INSIDE
        mRotateDetector = RotateGestureDetector(mRotateListener)
        mDetector = GestureDetector(context, mGestureListener)
        mScaleDetector = ScaleGestureDetector(context, mScaleListener)
        val density = resources.displayMetrics.density
        MAX_OVER_SCROLL = (density * 30).toInt()
        MAX_FLING_OVER_SCROLL = (density * 30).toInt()
        MAX_OVER_RESISTANCE = (density * 140).toInt()
        mMinRotate = MIN_ROTATE
        animaDuring = defaultAnimaDuring
        maxScale = MAX_SCALE
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        mClickListener = l
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (scaleType == ScaleType.MATRIX) return
        if (scaleType != mScaleType) {
            mScaleType = scaleType
            if (isInit) {
                initBase()
            }
        }
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        mLongClick = l
    }

    /**
     * 设置动画的插入器
     */
    fun setInterpolator(interpolator: Interpolator?) {
        mTranslate.setInterpolator(interpolator)
    }

    /**
     * 启用缩放功能
     */
    fun enable() {
        isEnable = true
    }

    /**
     * 禁用缩放功能
     */
    fun disenable() {
        isEnable = false
    }

    /**
     * 启用旋转功能
     */
    fun enableRotate() {
        isRotateEnable = true
    }

    /**
     * 禁用旋转功能
     */
    fun disableRotate() {
        isRotateEnable = false
    }

    /**
     */
    fun setMaxAnimFromWaiteTime(wait: Int) {
        MAX_ANIM_FROM_WAITE = wait
    }

    override fun setImageResource(resId: Int) {
        var drawable: Drawable? = null
        try {
            drawable = resources.getDrawable(resId)
        } catch (e: Exception) {
        }
        setImageDrawable(drawable)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable == null) {
            hasDrawable = false
            return
        }
        if (!hasSize(drawable)) return
        if (!hasDrawable) {
            hasDrawable = true
        }
        initBase()
    }

    private fun hasSize(d: Drawable): Boolean {
        return if ((d.intrinsicHeight <= 0 || d.intrinsicWidth <= 0)
            && (d.minimumWidth <= 0 || d.minimumHeight <= 0)
            && (d.bounds.width() <= 0 || d.bounds.height() <= 0)
        ) {
            false
        } else true
    }

    private fun initBase() {
        if (!hasDrawable) return
        if (!isKnowSize) return
        mBaseMatrix.reset()
        mAnimaMatrix.reset()
        isZoonUp = false
        val img = drawable
        val w = width
        val h = height
        val imgw = getDrawableWidth(img)
        val imgh = getDrawableHeight(img)
        mBaseRect[0f, 0f, imgw.toFloat()] = imgh.toFloat()

        // 以图片中心点居中位移
        val tx = (w - imgw) / 2
        val ty = (h - imgh) / 2
        var sx = 1f
        var sy = 1f

        // 缩放，默认不超过屏幕大小
        if (imgw > w) {
            sx = w.toFloat() / imgw
        }
        if (imgh > h) {
            sy = h.toFloat() / imgh
        }
        val scale = if (sx < sy) sx else sy
        mBaseMatrix.reset()
        mBaseMatrix.postTranslate(tx.toFloat(), ty.toFloat())
        mBaseMatrix.postScale(scale, scale, mScreenCenter.x, mScreenCenter.y)
        mBaseMatrix.mapRect(mBaseRect)
        mHalfBaseRectWidth = mBaseRect.width() / 2
        mHalfBaseRectHeight = mBaseRect.height() / 2
        mScaleCenter.set(mScreenCenter)
        mRotateCenter.set(mScaleCenter)
        executeTranslate()
        when (mScaleType) {
            ScaleType.CENTER -> initCenter()
            ScaleType.CENTER_CROP -> initCenterCrop()
            ScaleType.CENTER_INSIDE -> initCenterInside()
            ScaleType.FIT_CENTER -> initFitCenter()
            ScaleType.FIT_START -> initFitStart()
            ScaleType.FIT_END -> initFitEnd()
            ScaleType.FIT_XY -> initFitXY()
        }
        isInit = true
        if (mFromInfo != null && System.currentTimeMillis() - mInfoTime < MAX_ANIM_FROM_WAITE) {
            animaFrom(mFromInfo!!)
        }
        mFromInfo = null
    }

    private fun initCenter() {
        if (!hasDrawable) return
        if (!isKnowSize) return
        val img = drawable
        val imgw = getDrawableWidth(img)
        val imgh = getDrawableHeight(img)
        if (imgw > mWidgetRect.width() || imgh > mWidgetRect.height()) {
            val scaleX = imgw / mImgRect.width()
            val scaleY = imgh / mImgRect.height()
            mScale = if (scaleX > scaleY) scaleX else scaleY
            mAnimaMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y)
            executeTranslate()
            resetBase()
        }
    }

    private fun initCenterCrop() {
        if (mImgRect.width() < mWidgetRect.width() || mImgRect.height() < mWidgetRect.height()) {
            val scaleX = mWidgetRect.width() / mImgRect.width()
            val scaleY = mWidgetRect.height() / mImgRect.height()
            mScale = if (scaleX > scaleY) scaleX else scaleY
            mAnimaMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y)
            executeTranslate()
            resetBase()
        }
    }

    private fun initCenterInside() {
        if (mImgRect.width() > mWidgetRect.width() || mImgRect.height() > mWidgetRect.height()) {
            val scaleX = mWidgetRect.width() / mImgRect.width()
            val scaleY = mWidgetRect.height() / mImgRect.height()
            mScale = if (scaleX < scaleY) scaleX else scaleY
            mAnimaMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y)
            executeTranslate()
            resetBase()
        }
    }

    private fun initFitCenter() {
        if (mImgRect.width() < mWidgetRect.width()) {
            mScale = mWidgetRect.width() / mImgRect.width()
            mAnimaMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y)
            executeTranslate()
            resetBase()
        }
    }

    private fun initFitStart() {
        initFitCenter()
        val ty = -mImgRect.top
        mAnimaMatrix.postTranslate(0f, ty)
        executeTranslate()
        resetBase()
        mTranslateY += ty.toInt()
    }

    private fun initFitEnd() {
        initFitCenter()
        val ty = mWidgetRect.bottom - mImgRect.bottom
        mTranslateY += ty.toInt()
        mAnimaMatrix.postTranslate(0f, ty)
        executeTranslate()
        resetBase()
    }

    private fun initFitXY() {
        val scaleX = mWidgetRect.width() / mImgRect.width()
        val scaleY = mWidgetRect.height() / mImgRect.height()
        mAnimaMatrix.postScale(scaleX, scaleY, mScreenCenter.x, mScreenCenter.y)
        executeTranslate()
        resetBase()
    }

    private fun resetBase() {
        val img = drawable
        val imgw = getDrawableWidth(img)
        val imgh = getDrawableHeight(img)
        mBaseRect[0f, 0f, imgw.toFloat()] = imgh.toFloat()
        mBaseMatrix.set(mSynthesisMatrix)
        mBaseMatrix.mapRect(mBaseRect)
        mHalfBaseRectWidth = mBaseRect.width() / 2
        mHalfBaseRectHeight = mBaseRect.height() / 2
        mScale = 1f
        mTranslateX = 0
        mTranslateY = 0
        mAnimaMatrix.reset()
    }

    private fun executeTranslate() {
        mSynthesisMatrix.set(mBaseMatrix)
        mSynthesisMatrix.postConcat(mAnimaMatrix)
        imageMatrix = mSynthesisMatrix
        mAnimaMatrix.mapRect(mImgRect, mBaseRect)
        imgLargeWidth = mImgRect.width() > mWidgetRect.width()
        imgLargeHeight = mImgRect.height() > mWidgetRect.height()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!hasDrawable) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val d = drawable
        val drawableW = getDrawableWidth(d)
        val drawableH = getDrawableHeight(d)
        val pWidth = MeasureSpec.getSize(widthMeasureSpec)
        val pHeight = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var width = 0
        var height = 0
        var p = layoutParams
        if (p == null) {
            p = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        width = if (p.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                drawableW
            } else {
                pWidth
            }
        } else {
            if (widthMode == MeasureSpec.EXACTLY) {
                pWidth
            } else if (widthMode == MeasureSpec.AT_MOST) {
                if (drawableW > pWidth) pWidth else drawableW
            } else {
                drawableW
            }
        }
        height = if (p.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                drawableH
            } else {
                pHeight
            }
        } else {
            if (heightMode == MeasureSpec.EXACTLY) {
                pHeight
            } else if (heightMode == MeasureSpec.AT_MOST) {
                if (drawableH > pHeight) pHeight else drawableH
            } else {
                drawableH
            }
        }
        if (mAdjustViewBounds && drawableW.toFloat() / drawableH != width.toFloat() / height) {
            val hScale = height.toFloat() / drawableH
            val wScale = width.toFloat() / drawableW
            val scale = if (hScale < wScale) hScale else wScale
            width =
                if (p.width == ViewGroup.LayoutParams.MATCH_PARENT) width else (drawableW * scale).toInt()
            height =
                if (p.height == ViewGroup.LayoutParams.MATCH_PARENT) height else (drawableH * scale).toInt()
        }
        setMeasuredDimension(width, height)
    }

    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        super.setAdjustViewBounds(adjustViewBounds)
        mAdjustViewBounds = adjustViewBounds
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidgetRect[0f, 0f, w.toFloat()] = h.toFloat()
        mScreenCenter[w / 2.toFloat()] = h / 2.toFloat()
        if (!isKnowSize) {
            isKnowSize = true
            initBase()
        }
    }

    override fun draw(canvas: Canvas) {
        if (mClip != null) {
            canvas.clipRect(mClip!!)
            mClip = null
        }
        super.draw(canvas)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return if (isEnable) {
            val Action = event.actionMasked
            if (event.pointerCount >= 2) hasMultiTouch = true
            mDetector!!.onTouchEvent(event)
            if (isRotateEnable) {
                mRotateDetector!!.onTouchEvent(event)
            }
            mScaleDetector!!.onTouchEvent(event)
            if (Action == MotionEvent.ACTION_UP || Action == MotionEvent.ACTION_CANCEL) onUp()
            true
        } else {
            super.dispatchTouchEvent(event)
        }
    }

    private fun onUp() {
        if (mTranslate.isRuning) return
        if (canRotate || mDegrees % 90 != 0f) {
            var toDegrees = (mDegrees / 90).toInt() * 90.toFloat()
            val remainder = mDegrees % 90
            if (remainder > 45) toDegrees += 90f else if (remainder < -45) toDegrees -= 90f
            mTranslate.withRotate(mDegrees.toInt(), toDegrees.toInt())
            mDegrees = toDegrees
        }
        var scale = mScale
        if (mScale < 1) {
            scale = 1f
            mTranslate.withScale(mScale, 1f)
        } else if (mScale > maxScale) {
            scale = maxScale
            mTranslate.withScale(mScale, maxScale)
        }
        val cx = mImgRect.left + mImgRect.width() / 2
        val cy = mImgRect.top + mImgRect.height() / 2
        mScaleCenter[cx] = cy
        mRotateCenter[cx] = cy
        mTranslateX = 0
        mTranslateY = 0
        mTmpMatrix.reset()
        mTmpMatrix.postTranslate(-mBaseRect.left, -mBaseRect.top)
        mTmpMatrix.postTranslate(cx - mHalfBaseRectWidth, cy - mHalfBaseRectHeight)
        mTmpMatrix.postScale(scale, scale, cx, cy)
        mTmpMatrix.postRotate(mDegrees, cx, cy)
        mTmpMatrix.mapRect(mTmpRect, mBaseRect)
        doTranslateReset(mTmpRect)
        mTranslate.start()
    }

    private fun doTranslateReset(imgRect: RectF) {
        var tx = 0
        var ty = 0
        if (imgRect.width() <= mWidgetRect.width()) {
            if (!isImageCenterWidth(imgRect)) tx =
                (-((mWidgetRect.width() - imgRect.width()) / 2 - imgRect.left)).toInt()
        } else {
            if (imgRect.left > mWidgetRect.left) {
                tx = (imgRect.left - mWidgetRect.left).toInt()
            } else if (imgRect.right < mWidgetRect.right) {
                tx = (imgRect.right - mWidgetRect.right).toInt()
            }
        }
        if (imgRect.height() <= mWidgetRect.height()) {
            if (!isImageCenterHeight(imgRect)) ty =
                (-((mWidgetRect.height() - imgRect.height()) / 2 - imgRect.top)).toInt()
        } else {
            if (imgRect.top > mWidgetRect.top) {
                ty = (imgRect.top - mWidgetRect.top).toInt()
            } else if (imgRect.bottom < mWidgetRect.bottom) {
                ty = (imgRect.bottom - mWidgetRect.bottom).toInt()
            }
        }
        if (tx != 0 || ty != 0) {
            if (!mTranslate.mFlingScroller.isFinished) mTranslate.mFlingScroller.abortAnimation()
            mTranslate.withTranslate(mTranslateX, mTranslateY, -tx, -ty)
        }
    }

    private fun isImageCenterHeight(rect: RectF): Boolean {
        return Math.abs(Math.round(rect.top) - (mWidgetRect.height() - rect.height()) / 2) < 1
    }

    private fun isImageCenterWidth(rect: RectF): Boolean {
        return Math.abs(Math.round(rect.left) - (mWidgetRect.width() - rect.width()) / 2) < 1
    }

    private val mRotateListener: OnRotateListener =
        object : OnRotateListener {
            override fun onRotate(degrees: Float, focusX: Float, focusY: Float) {
                mRotateFlag += degrees
                if (canRotate) {
                    mDegrees += degrees
                    mAnimaMatrix.postRotate(degrees, focusX, focusY)
                } else {
                    if (Math.abs(mRotateFlag) >= mMinRotate) {
                        canRotate = true
                        mRotateFlag = 0f
                    }
                }
            }
        }
    private val mScaleListener: OnScaleGestureListener = object : OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            if (java.lang.Float.isNaN(scaleFactor) || java.lang.Float.isInfinite(scaleFactor)) return false
            mScale *= scaleFactor
            //            mScaleCenter.set(detector.getFocusX(), detector.getFocusY());
            mAnimaMatrix.postScale(
                scaleFactor,
                scaleFactor,
                detector.focusX,
                detector.focusY
            )
            executeTranslate()
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {}
    }

    private fun resistanceScrollByX(overScroll: Float, detalX: Float): Float {
        return detalX * (Math.abs(Math.abs(overScroll) - MAX_OVER_RESISTANCE) / MAX_OVER_RESISTANCE.toFloat())
    }

    private fun resistanceScrollByY(overScroll: Float, detalY: Float): Float {
        return detalY * (Math.abs(Math.abs(overScroll) - MAX_OVER_RESISTANCE) / MAX_OVER_RESISTANCE.toFloat())
    }

    /**
     * 匹配两个Rect的共同部分输出到out，若无共同部分则输出0，0，0，0
     */
    private fun mapRect(r1: RectF, r2: RectF, out: RectF) {
        val l: Float
        val r: Float
        val t: Float
        val b: Float
        l = if (r1.left > r2.left) r1.left else r2.left
        r = if (r1.right < r2.right) r1.right else r2.right
        if (l > r) {
            out[0f, 0f, 0f] = 0f
            return
        }
        t = if (r1.top > r2.top) r1.top else r2.top
        b = if (r1.bottom < r2.bottom) r1.bottom else r2.bottom
        if (t > b) {
            out[0f, 0f, 0f] = 0f
            return
        }
        out[l, t, r] = b
    }

    private fun checkRect() {
        if (!hasOverTranslate) {
            mapRect(mWidgetRect, mImgRect, mCommonRect)
        }
    }

    private val mClickRunnable = Runnable {
        if (mClickListener != null) {
            mClickListener!!.onClick(this@PhotoView)
        }
    }
    private val mGestureListener: GestureDetector.OnGestureListener =
        object : SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                if (mLongClick != null) {
                    mLongClick!!.onLongClick(this@PhotoView)
                }
            }

            override fun onDown(e: MotionEvent): Boolean {
                hasOverTranslate = false
                hasMultiTouch = false
                canRotate = false
                removeCallbacks(mClickRunnable)
                return false
            }

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (hasMultiTouch) return false
                if (!imgLargeWidth && !imgLargeHeight) return false
                if (mTranslate.isRuning) return false
                var vx = velocityX
                var vy = velocityY
                if (Math.round(mImgRect.left) >= mWidgetRect.left || Math.round(
                        mImgRect.right
                    ) <= mWidgetRect.right
                ) {
                    vx = 0f
                }
                if (Math.round(mImgRect.top) >= mWidgetRect.top || Math.round(
                        mImgRect.bottom
                    ) <= mWidgetRect.bottom
                ) {
                    vy = 0f
                }
                if (canRotate || mDegrees % 90 != 0f) {
                    var toDegrees = (mDegrees / 90).toInt() * 90.toFloat()
                    val remainder = mDegrees % 90
                    if (remainder > 45) toDegrees += 90f else if (remainder < -45) toDegrees -= 90f
                    mTranslate.withRotate(mDegrees.toInt(), toDegrees.toInt())
                    mDegrees = toDegrees
                }
                doTranslateReset(mImgRect)
                mTranslate.withFling(vx, vy)
                mTranslate.start()
                // onUp(e2);
                return super.onFling(e1, e2, velocityX, velocityY)
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                var distanceX = distanceX
                var distanceY = distanceY
                if (mTranslate.isRuning) {
                    mTranslate.stop()
                }
                if (canScrollHorizontallySelf(distanceX)) {
                    if (distanceX < 0 && mImgRect.left - distanceX > mWidgetRect.left) distanceX =
                        mImgRect.left
                    if (distanceX > 0 && mImgRect.right - distanceX < mWidgetRect.right) distanceX =
                        mImgRect.right - mWidgetRect.right
                    mAnimaMatrix.postTranslate(-distanceX, 0f)
                    mTranslateX -= distanceX.toInt()
                } else if (imgLargeWidth || hasMultiTouch || hasOverTranslate) {
                    checkRect()
                    if (!hasMultiTouch) {
                        if (distanceX < 0 && mImgRect.left - distanceX > mCommonRect.left) distanceX =
                            resistanceScrollByX(mImgRect.left - mCommonRect.left, distanceX)
                        if (distanceX > 0 && mImgRect.right - distanceX < mCommonRect.right) distanceX =
                            resistanceScrollByX(mImgRect.right - mCommonRect.right, distanceX)
                    }
                    mTranslateX -= distanceX.toInt()
                    mAnimaMatrix.postTranslate(-distanceX, 0f)
                    hasOverTranslate = true
                }
                if (canScrollVerticallySelf(distanceY)) {
                    if (distanceY < 0 && mImgRect.top - distanceY > mWidgetRect.top) distanceY =
                        mImgRect.top
                    if (distanceY > 0 && mImgRect.bottom - distanceY < mWidgetRect.bottom) distanceY =
                        mImgRect.bottom - mWidgetRect.bottom
                    mAnimaMatrix.postTranslate(0f, -distanceY)
                    mTranslateY -= distanceY.toInt()
                } else if (imgLargeHeight || hasOverTranslate || hasMultiTouch) {
                    checkRect()
                    if (!hasMultiTouch) {
                        if (distanceY < 0 && mImgRect.top - distanceY > mCommonRect.top) distanceY =
                            resistanceScrollByY(mImgRect.top - mCommonRect.top, distanceY)
                        if (distanceY > 0 && mImgRect.bottom - distanceY < mCommonRect.bottom) distanceY =
                            resistanceScrollByY(mImgRect.bottom - mCommonRect.bottom, distanceY)
                    }
                    mAnimaMatrix.postTranslate(0f, -distanceY)
                    mTranslateY -= distanceY.toInt()
                    hasOverTranslate = true
                }
                executeTranslate()
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                postDelayed(mClickRunnable, 250)
                return false
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                mTranslate.stop()
                var from = 1f
                var to = 1f
                val imgcx = mImgRect.left + mImgRect.width() / 2
                val imgcy = mImgRect.top + mImgRect.height() / 2
                mScaleCenter[imgcx] = imgcy
                mRotateCenter[imgcx] = imgcy
                mTranslateX = 0
                mTranslateY = 0
                if (isZoonUp) {
                    from = mScale
                    to = 1f
                } else {
                    from = mScale
                    to = maxScale
                    mScaleCenter[e.x] = e.y
                }
                mTmpMatrix.reset()
                mTmpMatrix.postTranslate(-mBaseRect.left, -mBaseRect.top)
                mTmpMatrix.postTranslate(mRotateCenter.x, mRotateCenter.y)
                mTmpMatrix.postTranslate(-mHalfBaseRectWidth, -mHalfBaseRectHeight)
                mTmpMatrix.postRotate(mDegrees, mRotateCenter.x, mRotateCenter.y)
                mTmpMatrix.postScale(to, to, mScaleCenter.x, mScaleCenter.y)
                mTmpMatrix.postTranslate(mTranslateX.toFloat(), mTranslateY.toFloat())
                mTmpMatrix.mapRect(mTmpRect, mBaseRect)
                doTranslateReset(mTmpRect)
                isZoonUp = !isZoonUp
                mTranslate.withScale(from, to)
                mTranslate.start()
                return false
            }
        }

    fun canScrollHorizontallySelf(direction: Float): Boolean {
        if (mImgRect.width() <= mWidgetRect.width()) return false
        if (direction < 0 && Math.round(mImgRect.left) - direction >= mWidgetRect.left) return false
        return if (direction > 0 && Math.round(mImgRect.right) - direction <= mWidgetRect.right) false else true
    }

    fun canScrollVerticallySelf(direction: Float): Boolean {
        if (mImgRect.height() <= mWidgetRect.height()) return false
        if (direction < 0 && Math.round(mImgRect.top) - direction >= mWidgetRect.top) return false
        return if (direction > 0 && Math.round(mImgRect.bottom) - direction <= mWidgetRect.bottom) false else true
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return if (hasMultiTouch) true else canScrollHorizontallySelf(direction.toFloat())
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return if (hasMultiTouch) true else canScrollVerticallySelf(direction.toFloat())
    }

    private inner class InterpolatorProxy :
        Interpolator {
        private var mTarget: Interpolator?
        fun setTargetInterpolator(interpolator: Interpolator?) {
            mTarget = interpolator
        }

        override fun getInterpolation(input: Float): Float {
            return if (mTarget != null) {
                mTarget!!.getInterpolation(input)
            } else input
        }

        init {
            mTarget = DecelerateInterpolator()
        }
    }

    private inner class Transform internal constructor() : Runnable {
        var isRuning = false
        var mTranslateScroller: OverScroller
        var mFlingScroller: OverScroller
        var mScaleScroller: Scroller
        var mClipScroller: Scroller
        var mRotateScroller: Scroller
        var C: ClipCalculate? = null
        var mLastFlingX = 0
        var mLastFlingY = 0
        var mLastTranslateX = 0
        var mLastTranslateY = 0
        var mClipRect = RectF()
        var mInterpolatorProxy = InterpolatorProxy()
        fun setInterpolator(interpolator: Interpolator?) {
            mInterpolatorProxy.setTargetInterpolator(interpolator)
        }

        fun withTranslate(startX: Int, startY: Int, deltaX: Int, deltaY: Int) {
            mLastTranslateX = 0
            mLastTranslateY = 0
            mTranslateScroller.startScroll(0, 0, deltaX, deltaY, animaDuring)
        }

        fun withScale(form: Float, to: Float) {
            mScaleScroller.startScroll(
                (form * 10000).toInt(),
                0,
                ((to - form) * 10000).toInt(),
                0,
                animaDuring
            )
        }

        fun withClip(
            fromX: Float,
            fromY: Float,
            deltaX: Float,
            deltaY: Float,
            d: Int,
            c: ClipCalculate?
        ) {
            mClipScroller.startScroll(
                (fromX * 10000).toInt(),
                (fromY * 10000).toInt(),
                (deltaX * 10000).toInt(),
                (deltaY * 10000).toInt(),
                d
            )
            C = c
        }

        fun withRotate(fromDegrees: Int, toDegrees: Int) {
            mRotateScroller.startScroll(fromDegrees, 0, toDegrees - fromDegrees, 0, animaDuring)
        }

        fun withRotate(fromDegrees: Int, toDegrees: Int, during: Int) {
            mRotateScroller.startScroll(fromDegrees, 0, toDegrees - fromDegrees, 0, during)
        }

        fun withFling(velocityX: Float, velocityY: Float) {
            mLastFlingX = if (velocityX < 0) Int.MAX_VALUE else 0
            var distanceX =
                (if (velocityX > 0) Math.abs(mImgRect.left) else mImgRect.right - mWidgetRect.right).toInt()
            distanceX = if (velocityX < 0) Int.MAX_VALUE - distanceX else distanceX
            var minX = if (velocityX < 0) distanceX else 0
            var maxX = if (velocityX < 0) Int.MAX_VALUE else distanceX
            val overX = if (velocityX < 0) Int.MAX_VALUE - minX else distanceX
            mLastFlingY = if (velocityY < 0) Int.MAX_VALUE else 0
            var distanceY =
                (if (velocityY > 0) Math.abs(mImgRect.top) else mImgRect.bottom - mWidgetRect.bottom).toInt()
            distanceY = if (velocityY < 0) Int.MAX_VALUE - distanceY else distanceY
            var minY = if (velocityY < 0) distanceY else 0
            var maxY = if (velocityY < 0) Int.MAX_VALUE else distanceY
            val overY = if (velocityY < 0) Int.MAX_VALUE - minY else distanceY
            if (velocityX == 0f) {
                maxX = 0
                minX = 0
            }
            if (velocityY == 0f) {
                maxY = 0
                minY = 0
            }
            mFlingScroller.fling(
                mLastFlingX,
                mLastFlingY,
                velocityX.toInt(),
                velocityY.toInt(),
                minX,
                maxX,
                minY,
                maxY,
                if (Math.abs(overX) < MAX_FLING_OVER_SCROLL * 2) 0 else MAX_FLING_OVER_SCROLL,
                if (Math.abs(overY) < MAX_FLING_OVER_SCROLL * 2) 0 else MAX_FLING_OVER_SCROLL
            )
        }

        fun start() {
            isRuning = true
            postExecute()
        }

        fun stop() {
            removeCallbacks(this)
            mTranslateScroller.abortAnimation()
            mScaleScroller.abortAnimation()
            mFlingScroller.abortAnimation()
            mRotateScroller.abortAnimation()
            isRuning = false
        }

        override fun run() {

            // if (!isRuning) return;
            var endAnima = true
            if (mScaleScroller.computeScrollOffset()) {
                mScale = mScaleScroller.currX / 10000f
                endAnima = false
            }
            if (mTranslateScroller.computeScrollOffset()) {
                val tx = mTranslateScroller.currX - mLastTranslateX
                val ty = mTranslateScroller.currY - mLastTranslateY
                mTranslateX += tx
                mTranslateY += ty
                mLastTranslateX = mTranslateScroller.currX
                mLastTranslateY = mTranslateScroller.currY
                endAnima = false
            }
            if (mFlingScroller.computeScrollOffset()) {
                val x = mFlingScroller.currX - mLastFlingX
                val y = mFlingScroller.currY - mLastFlingY
                mLastFlingX = mFlingScroller.currX
                mLastFlingY = mFlingScroller.currY
                mTranslateX += x
                mTranslateY += y
                endAnima = false
            }
            if (mRotateScroller.computeScrollOffset()) {
                mDegrees = mRotateScroller.currX.toFloat()
                endAnima = false
            }
            if (mClipScroller.computeScrollOffset() || mClip != null) {
                val sx = mClipScroller.currX / 10000f
                val sy = mClipScroller.currY / 10000f
                mTmpMatrix.setScale(
                    sx,
                    sy,
                    (mImgRect.left + mImgRect.right) / 2,
                    C!!.calculateTop()
                )
                mTmpMatrix.mapRect(mClipRect, mImgRect)
                if (sx == 1f) {
                    mClipRect.left = mWidgetRect.left
                    mClipRect.right = mWidgetRect.right
                }
                if (sy == 1f) {
                    mClipRect.top = mWidgetRect.top
                    mClipRect.bottom = mWidgetRect.bottom
                }
                mClip = mClipRect
            }
            if (!endAnima) {
                applyAnima()
                postExecute()
            } else {
                isRuning = false

                // 修复动画结束后边距有些空隙，
                var needFix = false
                if (imgLargeWidth) {
                    if (mImgRect.left > 0) {
                        mTranslateX -= mImgRect.left.toInt()
                    } else if (mImgRect.right < mWidgetRect.width()) {
                        mTranslateX -= (mWidgetRect.width() - mImgRect.right).toInt()
                    }
                    needFix = true
                }
                if (imgLargeHeight) {
                    if (mImgRect.top > 0) {
                        mTranslateY -= mImgRect.top.toInt()
                    } else if (mImgRect.bottom < mWidgetRect.height()) {
                        mTranslateY -= (mWidgetRect.height() - mImgRect.bottom).toInt()
                    }
                    needFix = true
                }
                if (needFix) {
                    applyAnima()
                }
                invalidate()
                if (mCompleteCallBack != null) {
                    mCompleteCallBack!!.run()
                    mCompleteCallBack = null
                }
            }
        }

        private fun applyAnima() {
            mAnimaMatrix.reset()
            mAnimaMatrix.postTranslate(-mBaseRect.left, -mBaseRect.top)
            mAnimaMatrix.postTranslate(mRotateCenter.x, mRotateCenter.y)
            mAnimaMatrix.postTranslate(-mHalfBaseRectWidth, -mHalfBaseRectHeight)
            mAnimaMatrix.postRotate(mDegrees, mRotateCenter.x, mRotateCenter.y)
            mAnimaMatrix.postScale(mScale, mScale, mScaleCenter.x, mScaleCenter.y)
            mAnimaMatrix.postTranslate(mTranslateX.toFloat(), mTranslateY.toFloat())
            executeTranslate()
        }

        private fun postExecute() {
            if (isRuning) post(this)
        }

        init {
            val ctx = context
            mTranslateScroller = OverScroller(ctx, mInterpolatorProxy)
            mScaleScroller = Scroller(ctx, mInterpolatorProxy)
            mFlingScroller = OverScroller(ctx, mInterpolatorProxy)
            mClipScroller = Scroller(ctx, mInterpolatorProxy)
            mRotateScroller = Scroller(ctx, mInterpolatorProxy)
        }
    }

    val info: Info
        get() {
            val rect = RectF()
            val p = IntArray(2)
            getLocation(this, p)
            rect[p[0] + mImgRect.left, p[1] + mImgRect.top, p[0] + mImgRect.right] =
                p[1] + mImgRect.bottom
            return Info(
                rect,
                mImgRect,
                mWidgetRect,
                mBaseRect,
                mScreenCenter,
                mScale,
                mDegrees,
                mScaleType!!
            )
        }

    private fun reset() {
        mAnimaMatrix.reset()
        executeTranslate()
        mScale = 1f
        mTranslateX = 0
        mTranslateY = 0
    }

    interface ClipCalculate {
        fun calculateTop(): Float
    }

    inner class START : ClipCalculate {
        override fun calculateTop(): Float {
            return mImgRect.top
        }
    }

    inner class END : ClipCalculate {
        override fun calculateTop(): Float {
            return mImgRect.bottom
        }
    }

    inner class OTHER : ClipCalculate {
        override fun calculateTop(): Float {
            return (mImgRect.top + mImgRect.bottom) / 2
        }
    }

    /**
     * 在PhotoView内部还没有图片的时候同样可以调用该方法
     *
     *
     * 此时并不会播放动画，当给PhotoView设置图片后会自动播放动画。
     *
     *
     * 若等待时间过长也没有给控件设置图片，则会忽略该动画，若要再次播放动画则需要重新调用该方法
     * (等待的时间默认500毫秒，可以通过setMaxAnimFromWaiteTime(int)设置最大等待时间)
     */
    fun animaFrom(info: Info) {
        if (isInit) {
            reset()
            val mine = info
            val scaleX = info.mImgRect.width() / mine.mImgRect.width()
            val scaleY = info.mImgRect.height() / mine.mImgRect.height()
            val scale = if (scaleX < scaleY) scaleX else scaleY
            val ocx = info.mRect.left + info.mRect.width() / 2
            val ocy = info.mRect.top + info.mRect.height() / 2
            val mcx = mine.mRect.left + mine.mRect.width() / 2
            val mcy = mine.mRect.top + mine.mRect.height() / 2
            mAnimaMatrix.reset()
            // mAnimaMatrix.postTranslate(-mBaseRect.left, -mBaseRect.top);
            mAnimaMatrix.postTranslate(ocx - mcx, ocy - mcy)
            mAnimaMatrix.postScale(scale, scale, ocx, ocy)
            mAnimaMatrix.postRotate(info.mDegrees, ocx, ocy)
            executeTranslate()
            mScaleCenter[ocx] = ocy
            mRotateCenter[ocx] = ocy
            mTranslate.withTranslate(0, 0, (-(ocx - mcx)).toInt(), (-(ocy - mcy)).toInt())
            mTranslate.withScale(scale, 1f)
            mTranslate.withRotate(info.mDegrees.toInt(), 0)
            if (info.mWidgetRect.width() < info.mImgRect.width() || info.mWidgetRect.height() < info.mImgRect.height()) {
                var clipX = info.mWidgetRect.width() / info.mImgRect.width()
                var clipY = info.mWidgetRect.height() / info.mImgRect.height()
                clipX = if (clipX > 1) 1F else clipX
                clipY = if (clipY > 1) 1F else clipY
                val c =
                    if (info.mScaleType == ScaleType.FIT_START) START() else if (info.mScaleType == ScaleType.FIT_END) END() else OTHER()
                mTranslate.withClip(clipX, clipY, 1 - clipX, 1 - clipY, animaDuring / 3, c)
                mTmpMatrix.setScale(
                    clipX,
                    clipY,
                    (mImgRect.left + mImgRect.right) / 2,
                    c.calculateTop()
                )
                mTmpMatrix.mapRect(mTranslate.mClipRect, mImgRect)
                mClip = mTranslate.mClipRect
            }
            mTranslate.start()
        } else {
            mFromInfo = info
            mInfoTime = System.currentTimeMillis()
        }
    }

    fun animaTo(
        info: Info,
        completeCallBack: Runnable?
    ) {
        if (isInit) {
            mTranslate.stop()
            mTranslateX = 0
            mTranslateY = 0
            val tcx = info.mRect.left + info.mRect.width() / 2
            val tcy = info.mRect.top + info.mRect.height() / 2
            mScaleCenter[mImgRect.left + mImgRect.width() / 2] =
                mImgRect.top + mImgRect.height() / 2
            mRotateCenter.set(mScaleCenter)

            // 将图片旋转回正常位置，用以计算
            mAnimaMatrix.postRotate(-mDegrees, mScaleCenter.x, mScaleCenter.y)
            mAnimaMatrix.mapRect(mImgRect, mBaseRect)

            // 缩放
            val scaleX = info.mImgRect.width() / mBaseRect.width()
            val scaleY = info.mImgRect.height() / mBaseRect.height()
            val scale = if (scaleX > scaleY) scaleX else scaleY
            mAnimaMatrix.postRotate(mDegrees, mScaleCenter.x, mScaleCenter.y)
            mAnimaMatrix.mapRect(mImgRect, mBaseRect)
            mDegrees = mDegrees % 360
            mTranslate.withTranslate(
                0,
                0,
                (tcx - mScaleCenter.x).toInt(),
                (tcy - mScaleCenter.y).toInt()
            )
            mTranslate.withScale(mScale, scale)
            mTranslate.withRotate(mDegrees.toInt(), info.mDegrees.toInt(), animaDuring * 2 / 3)
            if (info.mWidgetRect.width() < info.mRect.width() || info.mWidgetRect.height() < info.mRect.height()) {
                var clipX = info.mWidgetRect.width() / info.mRect.width()
                var clipY = info.mWidgetRect.height() / info.mRect.height()
                clipX = if (clipX > 1) 1F else clipX
                clipY = if (clipY > 1) 1F else clipY
                val cx = clipX
                val cy = clipY
                val c =
                    if (info.mScaleType == ScaleType.FIT_START) START() else if (info.mScaleType == ScaleType.FIT_END) END() else OTHER()
                postDelayed(object : Runnable {
                    override fun run() {
                        mTranslate.withClip(1f, 1f, -1 + cx, -1 + cy, animaDuring / 2, c)
                    }
                }, animaDuring / 2.toLong())
            }
            mCompleteCallBack = completeCallBack
            mTranslate.start()
        }
    }

    fun rotate(degrees: Float) {
        mDegrees += degrees
        val centerX = (mWidgetRect.left + mWidgetRect.width() / 2).toInt()
        val centerY = (mWidgetRect.top + mWidgetRect.height() / 2).toInt()
        mAnimaMatrix.postRotate(degrees, centerX.toFloat(), centerY.toFloat())
        executeTranslate()
    }

    companion object {
        private const val MIN_ROTATE = 35

        /**
         * 获取默认的动画持续时间
         */
        const val defaultAnimaDuring = 340
        private const val MAX_SCALE = 2.5f
        private fun getDrawableWidth(d: Drawable): Int {
            var width = d.intrinsicWidth
            if (width <= 0) width = d.minimumWidth
            if (width <= 0) width = d.bounds.width()
            return width
        }

        private fun getDrawableHeight(d: Drawable): Int {
            var height = d.intrinsicHeight
            if (height <= 0) height = d.minimumHeight
            if (height <= 0) height = d.bounds.height()
            return height
        }

        fun getImageViewInfo(imgView: ImageView): Info {
            val p = IntArray(2)
            getLocation(imgView, p)
            val drawable = imgView.drawable
            val matrix = imgView.imageMatrix
            val width = getDrawableWidth(drawable)
            val height = getDrawableHeight(drawable)
            val imgRect = RectF(0F, 0F, width.toFloat(), height.toFloat())
            matrix.mapRect(imgRect)
            val rect = RectF(
                p[0] + imgRect.left,
                p[1] + imgRect.top,
                p[0] + imgRect.right,
                p[1] + imgRect.bottom
            )
            val widgetRect = RectF(
                0F, 0F, imgView.width.toFloat(), imgView.height
                    .toFloat()
            )
            val baseRect = RectF(widgetRect)
            val screenCenter = PointF(widgetRect.width() / 2, widgetRect.height() / 2)
            return Info(
                rect,
                imgRect,
                widgetRect,
                baseRect,
                screenCenter,
                1F,
                0F,
                imgView.scaleType
            )
        }

        private fun getLocation(target: View, position: IntArray) {
            position[0] += target.left
            position[1] += target.top
            var viewParent = target.parent
            while (viewParent is View) {
                val view = viewParent as View
                if (view.id == R.id.content) return
                position[0] -= view.scrollX
                position[1] -= view.scrollY
                position[0] += view.left
                position[1] += view.top
                viewParent = view.parent
            }
            position[0] = (position[0] + 0.5f).toInt()
            position[1] = (position[1] + 0.5f).toInt()
        }
    }
}