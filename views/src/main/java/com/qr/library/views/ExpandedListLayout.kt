package com.qr.library.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ExpandedListLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attributeSet, defStyleAttr) {
    var hostView: View? = null
    var dividerView: View? = null
    var subView: View? = null
    var subDividerView: View? = null
    var subCount: Int = 1

    init {
        val inflater = LayoutInflater.from(context)
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.ExpandedListLayout)
        val hostViewId = typedArray.getResourceId(R.styleable.ExpandedListLayout_ell_hostView, -1)
        if (hostViewId >= 0) {
            hostView = inflater.inflate(hostViewId, this, false)
        }
        val dividerViewId = typedArray.getResourceId(R.styleable.ExpandedListLayout_ell_divider, -1)
        if (dividerViewId >= 0) {
            dividerView = inflater.inflate(dividerViewId, this, false)
        }
        val subViewId = typedArray.getResourceId(R.styleable.ExpandedListLayout_ell_subView, -1)
        if (subViewId >= 0) {
            subView = inflater.inflate(subViewId, this, false)
        }
        val subDividerId =
            typedArray.getResourceId(R.styleable.ExpandedListLayout_ell_subDivider, -1)
        if (subDividerId >= 0) {
            subDividerView = inflater.inflate(subDividerId, this, false)
        }
        subCount = typedArray.getInteger(R.styleable.ExpandedListLayout_ell_subCount, 1)

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // first measure hostView size
        hostView?.let {

        }
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        TODO("Not yet implemented")
    }
}