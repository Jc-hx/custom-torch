package com.chx.custom_torch

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.graphics.drawable.Drawable


class VerticalSeekBar : androidx.appcompat.widget.AppCompatSeekBar {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        // Draw tick marks manually
//        tickMark?.let {
//            val centerY = (height / 2).toFloat()
//            val drawableHeight = it.intrinsicHeight
//            val halfDrawableHeight = drawableHeight / 2
//            val drawableWidth = it.intrinsicWidth
//
//            val scale = height.toFloat() / (max - min)
//            for (i in 0 until max) {
//                val y = centerY - (i * scale)
//                val x = -halfDrawableHeight.toFloat() + 200F // Adjust the position as needed
//                it.setBounds(x.toInt(), y.toInt(), (x + drawableHeight).toInt(), (y + drawableWidth).toInt())
//                it.draw(c)
//            }
//        }

        c.rotate(-90f)
        c.translate(-height.toFloat(), 0f)
        super.onDraw(c)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                progress = max - (max * event.y / height).toInt()
                onSizeChanged(width, height, 0, 0)
            }

            MotionEvent.ACTION_CANCEL -> {}
        }
        return true
    }
}
