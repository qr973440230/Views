package com.qr.library.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class CropView(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var path: String? = null
    private var bitmap: Bitmap? = null
    private var srcHeight = 0
    private var srcWidth = 0

    // 显示区域
    private var clipRect: Rect? = null

    // 裁剪框
    private var cropRect: Rect? = null

    // 图片绘制框
    private var imageSrcRect: Rect? = null
    private var viewWidth = 0
    private var viewHeight = 0
    private var viewLeft = 0
    private var viewTop = 0
    private val circleRadius = 20
    private var touchStartX = 0
    private var touchStartY = 0
    private var touchType = 0
    private var touched = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (clipRect == null || cropRect == null || bitmap == null) {
            return false
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            touched = true
            touchStartX = event.x.toInt()
            touchStartY = event.y.toInt()
            val touchStep = cropRect!!.width() / 6
            touchType = if (abs(touchStartX - cropRect!!.left) < touchStep && abs(
                    touchStartY - cropRect!!.top
                ) < touchStep
            ) {
                0
                // left-top
            } else if (abs(touchStartX - cropRect!!.left) < touchStep && abs(
                    touchStartY - cropRect!!.bottom
                ) < touchStep
            ) {
                1
                // left-bottom
            } else if (abs(touchStartX - cropRect!!.right) < touchStep && abs(
                    touchStartY - cropRect!!.top
                ) < touchStep
            ) {
                2
                // right-top
            } else if (abs(touchStartX - cropRect!!.right) < touchStep && abs(
                    touchStartY - cropRect!!.bottom
                ) < touchStep
            ) {
                3
                // right-bottom
            } else if (Rect(
                    cropRect!!.left + touchStep * 2,
                    cropRect!!.top + touchStep * 2,
                    cropRect!!.left + touchStep * 4,
                    cropRect!!.top + touchStep * 4
                ).contains(touchStartX, touchStartY)
            ) { // center
                4
            } else { // Other
                5
            }
        } else if (event.action == MotionEvent.ACTION_UP) {
            touched = false
            if (viewLeft > cropRect!!.left) {
                viewLeft = cropRect!!.left
            }
            if (viewLeft + viewWidth < cropRect!!.right) {
                viewLeft = cropRect!!.right - viewWidth
            }
            if (viewTop > cropRect!!.top) {
                viewTop = cropRect!!.top
            }
            if (viewTop + viewHeight < cropRect!!.bottom) {
                viewTop = cropRect!!.bottom - viewHeight
            }
        } else if (event.action == MotionEvent.ACTION_CANCEL) {
            touched = false
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            if (touchType == 5) {
                viewLeft += (event.x - touchStartX).toInt()
                viewTop += (event.y - touchStartY).toInt()
            } else if (touchType == 0) { // left-top
                val deltaX = event.x - touchStartX
                val deltaY = event.y - touchStartY
                if (abs(deltaX) > abs(deltaY)) {
                    val resultLeft = (cropRect!!.left + deltaX).toInt()
                    val resultTop = (cropRect!!.top + deltaX).toInt()
                    if (resultLeft >= circleRadius / 2 && resultTop >= circleRadius / 2 && cropRect!!.right - resultLeft >= 256 && cropRect!!.bottom - resultTop >= 256
                    ) {
                        cropRect!!.left = resultLeft
                        cropRect!!.top = cropRect!!.bottom - cropRect!!.width()
                    }
                } else {
                    val resultLeft = (cropRect!!.left + deltaY).toInt()
                    val resultTop = (cropRect!!.top + deltaY).toInt()
                    if (resultLeft >= circleRadius / 2 && resultTop >= circleRadius / 2 && cropRect!!.right - resultLeft >= 256 && cropRect!!.bottom - resultTop >= 256
                    ) {
                        cropRect!!.left = resultLeft
                        cropRect!!.top = cropRect!!.bottom - cropRect!!.width()
                    }
                }
            } else if (touchType == 1) { // left-bottom
                val deltaX = event.x - touchStartX
                val deltaY = event.y - touchStartY
                if (abs(deltaX) > abs(deltaY)) {
                    val resultLeft = (cropRect!!.left + deltaX).toInt()
                    val resultBottom = (cropRect!!.bottom - deltaX).toInt()
                    if (resultLeft >= circleRadius / 2
                        && srcHeight - resultBottom >= circleRadius / 2
                        && cropRect!!.right - resultLeft >= 256
                        && resultBottom - cropRect!!.top >= 256
                    ) {
                        cropRect!!.left = resultLeft
                        cropRect!!.bottom = cropRect!!.top + cropRect!!.width()
                    }
                } else {
                    val resultLeft = (cropRect!!.left - deltaY).toInt()
                    val resultBottom = (cropRect!!.bottom + deltaY).toInt()
                    if (resultLeft >= circleRadius / 2
                        && srcHeight - resultBottom >= circleRadius / 2
                        && cropRect!!.right - resultLeft >= 256
                        && resultBottom - cropRect!!.top >= 256
                    ) {
                        cropRect!!.left = resultLeft
                        cropRect!!.bottom = cropRect!!.top + cropRect!!.width()
                    }
                }
            } else if (touchType == 2) { // right-top
                val deltaY = event.y - touchStartY
                val deltaX = event.x - touchStartX
                if (abs(deltaX) > abs(deltaY)) {
                    val resultRight = (cropRect!!.right + deltaX).toInt()
                    val resultTop = (cropRect!!.top - deltaX).toInt()
                    if (srcWidth - resultRight >= circleRadius / 2
                        && resultTop >= circleRadius / 2
                        && resultRight - cropRect!!.left >= 256
                        && cropRect!!.bottom - resultTop >= 256
                    ) {
                        cropRect!!.right = resultRight
                        cropRect!!.top = cropRect!!.bottom - cropRect!!.width()
                    }
                } else {
                    val resultRight = (cropRect!!.right - deltaY).toInt()
                    val resultTop = (cropRect!!.top + deltaY).toInt()
                    if (srcWidth - resultRight >= circleRadius / 2
                        && resultTop >= circleRadius / 2
                        && resultRight - cropRect!!.left >= 256
                        && cropRect!!.bottom - resultTop >= 256
                    ) {
                        cropRect!!.right = resultRight
                        cropRect!!.top = cropRect!!.bottom - cropRect!!.width()
                    }
                }
            } else if (touchType == 3) { // right-bottom
                val deltaY = event.y - touchStartY
                val deltaX = event.x - touchStartX
                if (abs(deltaX) > abs(deltaY)) {
                    val resultRight = (cropRect!!.right + deltaX).toInt()
                    val resultBottom = (cropRect!!.bottom + deltaX).toInt()
                    if (srcWidth - resultRight >= circleRadius / 2
                        && srcHeight - resultBottom >= circleRadius / 2
                        && resultRight - cropRect!!.left >= 256
                        && resultBottom - cropRect!!.top >= 256
                    ) {
                        cropRect!!.right = resultRight
                        cropRect!!.bottom = cropRect!!.top + cropRect!!.width()
                    }
                } else {
                    val resultRight = (cropRect!!.right + deltaY).toInt()
                    val resultBottom = (cropRect!!.bottom + deltaY).toInt()
                    if (srcWidth - resultRight >= circleRadius / 2
                        && srcHeight - resultBottom >= circleRadius / 2
                        && resultRight - cropRect!!.left >= 256
                        && resultBottom - cropRect!!.top >= 256
                    ) {
                        cropRect!!.right = resultRight
                        cropRect!!.bottom = cropRect!!.top + cropRect!!.width()
                    }
                }
            } else if (touchType == 4) { // center
                val deltaY = event.y - touchStartY
                val deltaX = event.x - touchStartX
                val resultLeft = (cropRect!!.left + deltaX).toInt()
                val resultRight = (cropRect!!.right + deltaX).toInt()
                val resultTop = (cropRect!!.top + deltaY).toInt()
                val resultBottom = (cropRect!!.bottom + deltaY).toInt()
                if (resultLeft >= circleRadius / 2
                    && resultTop >= circleRadius / 2
                    && srcWidth - resultRight >= circleRadius / 2
                    && srcHeight - resultBottom >= circleRadius / 2
                ) {
                    cropRect!!.left = resultLeft
                    cropRect!!.top = resultTop
                    cropRect!!.bottom = resultBottom
                    cropRect!!.right = resultRight
                }
            }
            touchStartX = event.x.toInt()
            touchStartY = event.y.toInt()
        }

        Log.d(TAG, cropRect!!.width().toString() + ":" + cropRect!!.height())

        invalidate()
        return true
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (clipRect == null || cropRect == null || bitmap == null) {
            return
        }
        val paint = Paint()
        paint.isAntiAlias = true
        canvas.save()
        // 绘制整张图
        canvas.save()
        canvas.clipRect(clipRect!!)
        val imageDstRect = Rect(
            viewLeft, viewTop,
            viewLeft + viewWidth,
            viewTop + viewHeight
        )
        canvas.drawBitmap(bitmap!!, imageSrcRect, imageDstRect, paint)
        canvas.restore()
        paint.setARGB(if (touched) 80 else 160, 0, 0, 0)
        canvas.drawRect(
            Rect(
                clipRect!!.left,
                clipRect!!.top,
                clipRect!!.right,
                cropRect!!.top
            ), paint
        )
        canvas.drawRect(
            Rect(
                clipRect!!.left,
                cropRect!!.bottom,
                clipRect!!.right,
                clipRect!!.bottom
            ), paint
        )
        canvas.drawRect(
            Rect(
                clipRect!!.left,
                cropRect!!.top,
                cropRect!!.left,
                cropRect!!.bottom
            ), paint
        )
        canvas.drawRect(
            Rect(
                cropRect!!.right,
                cropRect!!.top,
                clipRect!!.right,
                cropRect!!.bottom
            ), paint
        )
        drawGrid(canvas, cropRect!!)
        drawHandles(canvas, cropRect!!)
        canvas.restore()
    }

    private fun drawHandles(
        canvas: Canvas,
        cropRect: Rect
    ) {
        val paint = Paint()
        paint.isAntiAlias = false
        paint.setARGB(255, 255, 255, 255)
        val circleRadius2 = circleRadius / 2.0f
        canvas.drawOval(
            RectF(
                cropRect.left - circleRadius2, cropRect.top - circleRadius2,
                cropRect.left + circleRadius2, cropRect.top + circleRadius2
            ), paint
        )
        canvas.drawOval(
            RectF(
                cropRect.left - circleRadius2, cropRect.bottom - circleRadius2,
                cropRect.left + circleRadius2, cropRect.bottom + circleRadius2
            ), paint
        )
        canvas.drawOval(
            RectF(
                cropRect.right - circleRadius2, cropRect.top - circleRadius2,
                cropRect.right + circleRadius2, cropRect.top + circleRadius2
            ), paint
        )
        canvas.drawOval(
            RectF(
                cropRect.right - circleRadius2, cropRect.bottom - circleRadius2,
                cropRect.right + circleRadius2, cropRect.bottom + circleRadius2
            ), paint
        )
    }

    private fun drawGrid(canvas: Canvas, cropRect: Rect) {
        val paint = Paint()
        paint.isAntiAlias = false
        paint.setARGB(255, 255, 255, 255)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        val path = Path()
        path.moveTo(cropRect.left.toFloat(), cropRect.top.toFloat())
        path.lineTo(cropRect.right.toFloat(), cropRect.top.toFloat())
        path.lineTo(cropRect.right.toFloat(), cropRect.bottom.toFloat())
        path.lineTo(cropRect.left.toFloat(), cropRect.bottom.toFloat())
        path.lineTo(cropRect.left.toFloat(), cropRect.top.toFloat())
        if (touched) {
            val stepX = cropRect.width() / 3
            val stepY = cropRect.height() / 3
            for (i in 1..2) {
                path.moveTo(cropRect.left + i * stepX.toFloat(), cropRect.top.toFloat())
                path.lineTo(cropRect.left + i * stepX.toFloat(), cropRect.bottom.toFloat())
                path.moveTo(cropRect.left.toFloat(), cropRect.top + i * stepY.toFloat())
                path.lineTo(cropRect.right.toFloat(), cropRect.top + i * stepY.toFloat())
            }
        }
        canvas.drawPath(path, paint)
    }

    @SuppressLint("LogNotTimber")
    fun setPath(path: String?) {
        this.path = path
        post {
            bitmap = BitmapFactory.decodeFile(this.path)
            if (bitmap == null) {
                Log.d(TAG, "bitmap == null")
                return@post
            }
            srcWidth = width
            srcHeight = height
            clipRect = Rect(
                circleRadius / 2, circleRadius / 2,
                srcWidth - circleRadius / 2, srcHeight - circleRadius / 2
            )
            cropRect = Rect(
                clipRect!!.left, clipRect!!.top + (clipRect!!.height() - clipRect!!.width()) / 2,
                clipRect!!.right, clipRect!!.bottom - (clipRect!!.height() - clipRect!!.width()) / 2
            )
            val bitmapWidth = bitmap!!.width
            val bitmapHeight = bitmap!!.height
            imageSrcRect = Rect(0, 0, bitmapWidth, bitmapHeight)
            viewWidth = clipRect!!.width()
            viewHeight = clipRect!!.width() * bitmapHeight / bitmapWidth
            viewLeft = clipRect!!.left
            viewTop = clipRect!!.top + (clipRect!!.height() - viewHeight) / 2
            invalidate()
        }
    }

    val cropBitmap: Bitmap?
        @SuppressLint("LogNotTimber")
        get() = if (bitmap == null || cropRect == null) {
            null
        } else try {
            val scale =
                bitmap!!.width.toDouble() / viewWidth.toDouble()
            Bitmap.createBitmap(
                bitmap!!, ((cropRect!!.left - viewLeft) * scale).toInt(),
                ((cropRect!!.top - viewTop) * scale).toInt(),
                (cropRect!!.width() * scale).toInt(),
                (cropRect!!.height() * scale).toInt()
            )
        } catch (e: Exception) {
            Log.d(TAG, "CropBitmap Fail Reason: " + e.message)
            null
        }

    companion object {
        val TAG = CropView::class.java.simpleName
    }
}