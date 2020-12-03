package at.shockbytes.dante.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import at.shockbytes.dante.core.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.signin.DanteUser
import at.shockbytes.dante.ui.adapter.main.BookAdapterItem
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
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

fun FirebaseUser.toDanteUser(givenName: String? = this.displayName): DanteUser {
    return DanteUser(
        givenName,
        this.displayName,
        this.email,
        this.photoUrl,
        this.providerId,
        Tasks.await(this.getIdToken(false))?.token,
        this.uid
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

fun List<BookEntity>.toAdapterItems(): List<BookAdapterItem> {
    return this.map { entity ->
        BookAdapterItem.Book(entity)
    }
}