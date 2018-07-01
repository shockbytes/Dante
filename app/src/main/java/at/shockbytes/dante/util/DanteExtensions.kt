package at.shockbytes.dante.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Handler
import android.support.annotation.ColorInt
import android.support.design.widget.FloatingActionButton
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.inputmethod.InputMethodManager
import at.shockbytes.dante.signin.DanteUser
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author  Martin Macheiner
 * Date:    06-Jun-18.
 */


fun String.colored(@ColorInt color: Int): SpannableString {
    val spannable = SpannableString(this)
    spannable.setSpan(ForegroundColorSpan(color),
            0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return spannable
}

fun String.removeBrackets(): String {
    return this.replace("(", "")
            .replace(")", "")
}

fun FloatingActionButton.toggle(millis: Long = 300) {
    this.hide()
    Handler().postDelayed({ this.show() }, millis)
}

fun Uri.loadBitmap(context: Context): Single<Bitmap> {
    return Single.fromCallable {
        (Glide.with(context).load(this).submit().get() as BitmapDrawable).bitmap
    }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
}

fun Uri.loadRoundedBitmap(context: Context): Single<Bitmap> {
    return Single.fromCallable {
        (Glide.with(context).load(this).apply(RequestOptions.circleCropTransform()).submit().get() as BitmapDrawable).bitmap
    }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
}

fun GoogleSignInAccount.toDanteUser(): DanteUser {
    return DanteUser(this.givenName, this.displayName,
            this.email, this.photoUrl, "google",
            this.idToken)
}

fun Activity.hideKeyboard() {
    val view = this.currentFocus
    view?.let { v ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }
}
