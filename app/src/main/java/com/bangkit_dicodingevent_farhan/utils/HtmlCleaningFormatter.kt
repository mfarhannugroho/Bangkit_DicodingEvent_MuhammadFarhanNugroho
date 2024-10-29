package com.bangkit_dicodingevent_farhan.utils

import android.graphics.Typeface
import android.text.Html
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.core.text.toSpannable

object HtmlCleaningFormatter {
    fun String.cleanAndFormatHtml(): Spanned {
        val cleanText = this.replace(Regex("<img[^>]*>.*?</img>", RegexOption.MULTILINE), "")

        val spanned: Spanned = Html.fromHtml(cleanText, Html.FROM_HTML_MODE_LEGACY)
        val spannable = spanned.toSpannable()
        val spans = spannable.getSpans(0, spanned.length, Any::class.java)

        for (span in spans) {
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            val flags = spannable.getSpanFlags(span)
            if (span is StyleSpan) {
                spannable.removeSpan(span)
                spannable.setSpan(StyleSpan(span.style), start, end, flags)
            } else {
                spannable.setSpan(StyleSpan(Typeface.NORMAL), start, end, flags)
            }
        }
        return spannable
    }
}