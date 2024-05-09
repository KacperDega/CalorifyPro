package com.example.calorifypro

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Matcher
import java.util.regex.Pattern

class MealInputFilter : InputFilter {

    private val pattern: Pattern

    init {
        pattern = Pattern.compile("(\\d{0,6}(\\.\\d{0,2})?)?")
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val matcher: Matcher = pattern.matcher(dest)
        return if (!matcher.matches()) "" else {
            val inputText = dest.subSequence(0, dstart).toString() +
                    source.subSequence(start, end) +
                    dest.subSequence(dend, dest.length).toString()

            if (inputText.matches("(\\d{0,6}(\\.\\d{0,2})?)?".toRegex())) {
                source
            } else {
                ""
            }
        }
    }
}
