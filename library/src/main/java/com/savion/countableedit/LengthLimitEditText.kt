package com.savion.countableedit

import android.content.Context
import android.os.Build
import android.text.InputFilter
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class LengthLimitEditText : AppCompatEditText {
    private var max = 0
    private var outOfRangeCall: () -> Unit = {}

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    constructor(context: Context, max: Int, outOfRangeCall: () -> Unit) : super(context) {
        this.max = max
        this.outOfRangeCall = outOfRangeCall
        initEnternal()
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val t = context.obtainStyledAttributes(attrs, R.styleable.LengthLimitEditText)
        max = t.getInt(R.styleable.LengthLimitEditText_text_length_max, max)
        t.recycle()
        initEnternal()
    }

    private fun initEnternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusable = FOCUSABLE
        }
        isFocusableInTouchMode = true
        if (max > 0) {
            filters = arrayOf<InputFilter>(LimitLengthFilter(max, outOfRangeCall))
        }
    }
}
