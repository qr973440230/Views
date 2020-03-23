package com.qr.library.views

import android.content.Context
import android.util.TypedValue

internal object ViewUtils {
    /**
     * 将dp值转换为px值
     */
    fun dp2px(dpValue: Float, context: Context): Int {
        val scale = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            context.resources.displayMetrics
        )
        return scale.toInt()
    }
}