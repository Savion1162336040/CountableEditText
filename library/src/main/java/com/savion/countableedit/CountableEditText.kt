package com.savion.countableedit

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Editable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding

class CountableEditText : LinearLayout {
    private var outOfRangeCall: () -> Unit = {}
    private var maxLength: Int = 0
    private var cetText = ""
    private var cetHint = ""
    private var cetCountable = true
    private var countTextSize = 40f
    private var countTextColor = Color.BLACK
    private val textWatcher by lazy(LazyThreadSafetyMode.NONE) {
        object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                cetText = s.toString()
                updateCountContent(cetText)
            }
        }
    }

    private var drawableView: ImageView? = null
    private var countView: TextView? = null
    private val editView by lazy(LazyThreadSafetyMode.NONE) {
        LengthLimitEditText(context, maxLength) { outOfRangeCall() }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        var textColor = Color.BLACK
        var textSize = 40f
        var hintColor = Color.GRAY
        var hint = ""
        var text = ""
        var drawable: Drawable? = null
        var drawPadding = 0f
        var drawsize: Float = 0f
        attributeSet?.let {
            val t = context.obtainStyledAttributes(it, R.styleable.CountableEditText)
            textColor = t.getColor(R.styleable.CountableEditText_cet_textcolor, Color.BLACK)
            textSize = t.getDimension(R.styleable.CountableEditText_cet_textsize, 40f)
            hintColor = t.getColor(R.styleable.CountableEditText_cet_hintcolor, Color.GRAY)
            hint = t.getString(R.styleable.CountableEditText_cet_hint) ?: ""
            text = t.getString(R.styleable.CountableEditText_cet_text) ?: ""

            countTextColor =
                t.getColor(R.styleable.CountableEditText_cet_count_textcolor, Color.BLACK)
            countTextSize = t.getDimension(R.styleable.CountableEditText_cet_count_textsize, 40f)
            drawable = t.getDrawable(R.styleable.CountableEditText_cet_drawstart)
            drawPadding = t.getDimension(R.styleable.CountableEditText_cet_drawpadding, 0f)
            drawsize = t.getDimension(R.styleable.CountableEditText_cet_drawsize, drawsize)
            maxLength = t.getInt(R.styleable.CountableEditText_cet_max_length, 0)
            cetCountable = t.getBoolean(R.styleable.CountableEditText_cet_countable, cetCountable)
            t.recycle()
        }

        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        drawable?.let {
            drawableView = ImageView(context).apply {
                setImageDrawable(it)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            addView(
                drawableView,
                LayoutParams(
                    drawsize.toInt(),
                    drawsize.toInt()
                )
            )
        }

        cetText = text
        editView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        editView.setTextColor(textColor)
        editView.setHintTextColor(hintColor)
        editView.setHint(hint)
        editView.maxLines = 1
        editView.isSingleLine = true
        editView.setText(text)
        editView.setBackgroundResource(0)
        editView.setPadding(0)
        editView.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        addView(editView, LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT).apply {
            weight = 1f
            leftMargin = drawPadding.toInt()
            rightMargin = drawPadding.toInt()
        })
        editView.addTextChangedListener(textWatcher)

        setCountable(cetCountable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        editView.removeTextChangedListener(textWatcher)
    }

    private fun updateCountContent(text: String?) {
        countView?.let { view ->
            val current = text?.takeIf {
                it.isNotEmpty()
            }?.let {
                minOf(it.length, maxLength)
            } ?: 0
            view.text = "${current}/${maxLength}"
        }
    }

    fun setCountable(enable: Boolean) {
        this.cetCountable = enable
        if (cetCountable && countView == null) {
            countView = TextView(context)
            countView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, countTextSize)
            countView?.setTextColor(countTextColor)

            updateCountContent(cetText)
            addView(
                countView,
                LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    fun getCetHint(): String? {
        return editView.hint?.toString()
    }

    fun setCetHint(hint: String) {
        hint.takeIf {
            it.isNotEmpty()
        }?.let {
            this.cetHint = it
            editView.hint = it
        }
    }

    fun getCetText(): String? {
        return editView.text?.toString()
    }

    fun setCetText(text: String) {
        text.takeIf {
            it.isNotEmpty()
        }?.let {
            this.cetText = it
            editView.setText(it)
        }
    }


}