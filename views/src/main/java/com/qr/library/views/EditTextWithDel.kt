package com.qr.library.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.qr.library.utils.DisplayUtils

class EditTextWithDel : AppCompatEditText {
    private val imgDel: Drawable? = ContextCompat.getDrawable(context, R.drawable.et_clear)

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    init {
        addTextChangedListener {
            setDrawable()
        }
        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setDrawable()
            } else {
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }
        }
    }

    private fun setDrawable() {
        if (length() < 1) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        } else {
            setCompoundDrawablesWithIntrinsicBounds(null, null, imgDel, null)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP && isFocused && length() > 0) {
            val rect = Rect()
            getGlobalVisibleRect(rect)
            rect.left = rect.right - DisplayUtils.dp2px(24f, context)
            if (rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                setText("")
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}