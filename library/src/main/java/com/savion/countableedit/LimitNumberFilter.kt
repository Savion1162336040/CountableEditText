package com.savion.countableedit

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils

/**
 * @Author: savion
 * @date: 2021/12/22 16:19
 * @desc: 限制数字textFilter
 */
class LimitNumberFilter(
    private val max: Int,
    private val min: Int,
    private val outOfRangeCall: () -> Unit
) : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val destStart = dest.subSequence(0, dstart)
        val destEnd = dest.subSequence(dend, dest.length)
        val ddd = destStart.toString() + source.toString() + destEnd.toString()
        if (TextUtils.isEmpty(ddd)) {
            return source
        } else {
            val targetNum: Int = "${destStart}${source}${destEnd}".toIntOrNull() ?: 0
            if (targetNum > max || targetNum < min) {
                outOfRangeCall()
                return ""
            }
            return source
        }
    }
}
