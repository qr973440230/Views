package com.qr.library.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.qr.library.views.ViewUtils.dp2px

class EditTextWithVisible @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attributeSet, defStyleAttr) {
    private val imgVisible: Drawable? = ContextCompat.getDrawable(context, R.drawable.et_visibility)
    private val imgVisibleOff: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.et_visibility_off)

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
            if (inputType.and(InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, imgVisible, null)
            } else {
                setCompoundDrawablesWithIntrinsicBounds(null, null, imgVisibleOff, null)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP && isFocused && length() > 0) {
            val rect = Rect()
            getGlobalVisibleRect(rect)
            rect.left = rect.right - dp2px(24f, context)
            if (rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                if (inputType.and(InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
                    inputType = InputType.TYPE_CLASS_TEXT
                    setCompoundDrawablesWithIntrinsicBounds(null, null, imgVisibleOff, null)
                } else {
                    inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    setCompoundDrawablesWithIntrinsicBounds(null, null, imgVisible, null)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}