package at.shockbytes.dante.util

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.res.ResourcesCompat
import at.shockbytes.dante.core.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.login.DanteUser
import at.shockbytes.dante.ui.adapter.main.BookAdapterItem
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

fun FloatingActionButton.toggle(millis: Long = 300) {
    if (isExpanded) {
        isExpanded = false
    } else {
        hide()
        Handler(Looper.getMainLooper()).postDelayed({ show() }, millis)
    }
}

fun GoogleSignInAccount.toDanteUser(): DanteUser {
    return DanteUser(
        this.givenName,
        this.displayName,
        this.email,
        this.photoUrl,
        "google",
        this.idToken,
        userId = ""
    )
}

fun Context.shareFile(fileToPath: File): Intent {
    return Intent()
        .setAction(Intent.ACTION_SEND)
        .putExtra(Intent.EXTRA_TEXT, getString(R.string.share_file_template, fileToPath.name))
        .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileToPath))
        .setType("text/plain")
}

fun Context.openFile(fileToPath: File, mimeType: String): Intent {

    val uri = Uri.fromFile(fileToPath)

    return Intent()
        .setAction(Intent.ACTION_VIEW)
        .setDataAndType(uri, mimeType)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

fun Context.getThemeFont(): Typeface? {
    return ResourcesCompat.getFont(this, at.shockbytes.dante.R.font.nunito)
}

fun List<BookEntity>.toAdapterItems(): List<BookAdapterItem> {
    return this.map { entity ->
        BookAdapterItem.Book(entity)
    }
}

inline fun <reified T> Gson.listFromJson(data: String): List<T> {
    return this.fromJson(data, object : TypeToken<T>() {}.type)
}

inline fun <reified T> Gson.fromJson(data: String): T {
    return this.fromJson(data, object : TypeToken<T>() {}.type)
}