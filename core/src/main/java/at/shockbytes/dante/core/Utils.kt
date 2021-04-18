package at.shockbytes.dante.core

import android.os.Build
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

fun sdkVersionOrAbove(sdkVersion: Int): Boolean {
    return Build.VERSION.SDK_INT >= sdkVersion
}

fun <T> Single<T>.fromSingleToCompletable(): Completable {
    return Completable.fromSingle(this)
}