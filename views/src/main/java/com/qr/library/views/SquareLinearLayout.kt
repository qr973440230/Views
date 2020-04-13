package com.qr.library.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlin.math.min

class SquareLinearLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpec = min(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthSpec, widthSpec)
    }
}