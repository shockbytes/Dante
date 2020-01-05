package at.shockbytes.dante.util

import timber.log.Timber

object ExceptionHandlers {

    fun defaultExceptionHandler(throwable: Throwable) = Timber.e(throwable)
}