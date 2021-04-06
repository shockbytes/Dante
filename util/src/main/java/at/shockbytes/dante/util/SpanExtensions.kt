package at.shockbytes.dante.util

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.annotation.ColorInt

fun CharSequence.bold(): CharSequence {
    return SpannableString(this).apply {
        setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun CharSequence.colored(@ColorInt color: Int): CharSequence {
    return SpannableString(this).apply {
        setSpan(ForegroundColorSpan(color), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun CharSequence.link(onClickAction: () -> Unit): CharSequence {

    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) = onClickAction()
    }

    return SpannableString(this).apply {
        setSpan(clickableSpan, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun CharSequence.concat(vararg other: CharSequence): CharSequence {
    return listOf(this, *other).joinTo(SpannableStringBuilder(), separator = "")
}