package com.qr.library.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotlin.math.min

class SquareRelativeLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attributeSet, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpec = min(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthSpec, widthSpec)
    }
}