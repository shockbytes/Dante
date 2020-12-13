package at.shockbytes.dante.core

import android.os.Build

fun sdkVersionOrAbove(sdkVersion: Int): Boolean {
    return Build.VERSION.SDK_INT >= sdkVersion
}