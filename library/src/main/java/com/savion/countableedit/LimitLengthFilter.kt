package com.savion.countableedit

import android.text.InputFilter
import android.text.Spanned

/**
 * @author savion
 * @date 2025/5/14 17:25
 * @desc 限制长度textFilter
 */
class LimitLengthFilter(private val max: Int, private val outOfRangeCall: () -> Unit) :
    InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        var keep = max - (dest.length - (dend - dstart))
        if (keep <= 0) {
            outOfRangeCall()
            return ""
        } else if (keep >= end - start) {
            return null // keep original
        } else {
            keep += start
            if (Character.isHighSurrogate(source[keep - 1])) {
                --keep
                if (keep == start) {
                    outOfRangeCall()
                    return ""
                }
            }
            return source.subSequence(start, keep)
        }
    }
}
