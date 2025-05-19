package com.savion.countableedit

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Editable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams

class CountableEditText : ConstraintLayout {
    private var outOfRangeCall: () -> Unit = {}
    private var maxLength: Int = 0
    private var cetText = ""
    private var cetHint = ""
    private var cetCountable = true
    private var countTextSize = 40f
    private var countTextColor = Color.BLACK
    private var cetEditable = true
    private val textWatcher by lazy(LazyThreadSafetyMode.NONE) {
        object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                cetText = s.toString()
                updateCountContent(cetText)
                updateClear()
            }
        }
    }

    private val drawableView: ImageView
    private val countView: TextView
    private val editView: LengthLimitEditText
    private val clearView: ImageView
    private val topMask: View

    private var cetListener: CetListener? = null

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
        var clearDrawable: Drawable? = null
        var clearDrawableSizeWidth: Float = 0f
        var clearDrawableSizeHeight: Float = 0f
        var clearDrawablePaddingStart: Float = 0f
        var clearDrawablePaddingEnd: Float = 0f
        var clearDrawablePaddingTop: Float = 0f
        var clearDrawablePaddingBottom: Float = 0f
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
            cetEditable = t.getBoolean(R.styleable.CountableEditText_cet_editable, cetEditable)
            clearDrawable = t.getDrawable(R.styleable.CountableEditText_cet_clearDrawable)
            clearDrawableSizeWidth = t.getDimension(
                R.styleable.CountableEditText_cet_clearDrawableWidth,
                clearDrawableSizeWidth
            )
            clearDrawableSizeHeight = t.getDimension(
                R.styleable.CountableEditText_cet_clearDrawableHeight,
                clearDrawableSizeHeight
            )
            clearDrawablePaddingStart = t.getDimension(
                R.styleable.CountableEditText_cet_clearDrawablePaddingStart,
                clearDrawablePaddingStart
            )
            clearDrawablePaddingEnd = t.getDimension(
                R.styleable.CountableEditText_cet_clearDrawablePaddingEnd,
                clearDrawablePaddingEnd
            )
            clearDrawablePaddingTop = t.getDimension(
                R.styleable.CountableEditText_cet_clearDrawablePaddingTop,
                clearDrawablePaddingTop
            )
            clearDrawablePaddingBottom = t.getDimension(
                R.styleable.CountableEditText_cet_clearDrawablePaddingBottom,
                clearDrawablePaddingBottom
            )
            t.recycle()
        }
        val container =
            LayoutInflater.from(context).inflate(R.layout.layout_countable_edittext, this, true)
        drawableView = container.findViewById(R.id.countable_edittext_drawable)
        editView = container.findViewById(R.id.countable_edittext_input)
        countView = container.findViewById(R.id.countable_edittext_count)
        clearView = container.findViewById(R.id.countable_edittext_clear)
        topMask = container.findViewById(R.id.countable_edittext_topmask)

        drawable?.let {
            drawableView.visibility = View.VISIBLE
            drawableView.setImageDrawable(it)
        } ?: run {
            drawableView.visibility = View.GONE
        }
        drawableView.takeIf {
            drawsize > 0
        }?.let {
            it.updateLayoutParams<ViewGroup.LayoutParams> {
                width = drawsize.toInt()
                height = drawsize.toInt()
            }
        }

        clearView.setImageDrawable(clearDrawable)
        clearView.takeIf {
            clearDrawableSizeWidth > 0 && clearDrawableSizeHeight > 0
        }?.let {
            it.updateLayoutParams<ViewGroup.LayoutParams> {
                width = clearDrawableSizeWidth.toInt()
                height = clearDrawableSizeHeight.toInt()
            }
        }
        clearView.setPadding(
            clearDrawablePaddingStart.toInt(),
            clearDrawablePaddingTop.toInt(),
            clearDrawablePaddingEnd.toInt(),
            clearDrawablePaddingBottom.toInt()
        )
        clearView.setOnClickListener {
            editView.text = null
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
        editView.isEnabled = cetEditable
        editView.setPadding(0)
        editView.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        editView.addTextChangedListener(textWatcher)
        editView.updateLayoutParams<FrameLayout.LayoutParams> {
            leftMargin = drawPadding.toInt()
            rightMargin = drawPadding.toInt()
        }

        topMask.visibility = if (cetEditable) View.GONE else View.VISIBLE
        topMask.setOnClickListener {
            performClick()
        }

        setCountable(cetCountable)
        updateClear()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        editView.removeTextChangedListener(textWatcher)
    }

    private fun updateCountContent(text: String?) {
        countView.takeIf { view ->
            view.text != text
        }?.let { view ->
            val current = text?.takeIf {
                it.isNotEmpty()
            }?.let {
                minOf(it.length, maxLength)
            } ?: 0
            view.text = "${current}/${maxLength}"
            cetListener?.onTextChange(text)
        }
    }

    private fun updateClear() {
        clearView.takeIf {
            it.drawable != null
        }?.let {
            clearView.visibility = if (editView.text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    fun setCountable(enable: Boolean) {
        this.cetCountable = enable
        countView.setTextSize(TypedValue.COMPLEX_UNIT_PX, countTextSize)
        countView.setTextColor(countTextColor)
        countView.visibility = if (enable) View.VISIBLE else View.GONE
        updateCountContent(cetText)
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

    fun setCetDrawable(drawable: Drawable?) {
        drawableView.visibility = if (drawable != null) View.VISIBLE else View.GONE
        drawableView.setImageDrawable(drawable)
    }

    fun setCetListener(listener: CetListener) {
        this.cetListener = listener
    }

    interface CetListener {
        fun onTextChange(text: String?)
    }
}