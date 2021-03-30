package at.shockbytes.dante.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import androidx.annotation.ColorInt
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ArrayRes
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.util.AppUtils
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Author:  Martin Macheiner
 * Date:    06.06.2018
 */

fun Context.createRoundedBitmap(bitmap: Bitmap): RoundedBitmapDrawable {
    return AppUtils.createRoundedBitmap(this, bitmap)
}

fun String.removeBrackets(): String {
    return this
        .replace("(", "")
        .replace(")", "")
}

fun Activity.hideKeyboard() {
    val view = this.currentFocus
    view?.let { v ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }
}

fun FragmentManager.isFragmentShown(tag: String): Boolean {
    return findFragmentByTag(tag) != null
}

fun Fragment.showKeyboard(focusView: View) {
    showKeyboard(requireContext(), focusView)
}

fun showKeyboard(context: Context, view: View) {
    view.requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun View.setVisible(isVisible: Boolean, invisibilityState: Int = View.GONE) {
    val visibility = if (isVisible) View.VISIBLE else invisibilityState
    this.visibility = visibility
}

fun Double.roundDouble(digits: Int): Double {

    if (this == 0.0 || digits < 0 || this == Double.POSITIVE_INFINITY || this.isNaN() || this == Double.NEGATIVE_INFINITY) {
        return 0.00
    }

    return BigDecimal(this).setScale(digits, RoundingMode.HALF_UP).toDouble()
}

fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}

fun Fragment.isPortrait(): Boolean {
    return context?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT
}

fun Activity.isPortrait(): Boolean {
    return resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT
}

fun Activity.getStringList(@ArrayRes arrayRes: Int): List<String> {
    return resources.getStringArray(arrayRes).toList()
}

fun Fragment.getStringList(@ArrayRes arrayRes: Int): List<String> {
    return resources.getStringArray(arrayRes).toList()
}

fun Drawable.toBitmap(): Bitmap {

    if (this is BitmapDrawable) {
        return this.bitmap
    }

    val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)

    return bitmap
}

fun Context.isNightModeEnabled(): Boolean {
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun runDelayed(delay: Long, action: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        action()
    }, delay)
}

inline fun <reified T : ViewModel> Fragment.viewModelOf(factory: ViewModelProvider.Factory): T {
    return ViewModelProvider(this.viewModelStore, factory)[T::class.java]
}

inline fun <reified T : ViewModel> Fragment.lazyViewModelOf(factory: ViewModelProvider.Factory): Lazy<T> {
    return lazyOf(ViewModelProvider(this.viewModelStore, factory)[T::class.java])
}

inline fun <reified T : ViewModel> Fragment.viewModelOfActivity(activity: FragmentActivity, factory: ViewModelProvider.Factory): T {
    return ViewModelProvider(activity.viewModelStore, factory)[T::class.java]
}

inline fun <reified T : ViewModel> FragmentActivity.viewModelOf(factory: ViewModelProvider.Factory): T {
    return ViewModelProvider(this.viewModelStore, factory)[T::class.java]
}

fun <T> singleOf(
    subscribeOn: Scheduler? = null,
    observeOn: Scheduler? = null,
    block: () -> T
): Single<T> {

    var source = Single.fromCallable { block() }

    if (subscribeOn != null) {
        source = source.subscribeOn(subscribeOn)
    }

    if (observeOn != null) {
        source = source.observeOn(observeOn)
    }

    return source
}

fun Iterable<Completable>.merge() = Completable.merge(this)

fun completableOf(
    subscribeOn: Scheduler = Schedulers.io(),
    block: () -> Unit
): Completable {
    return Completable.fromAction(Action(block)).subscribeOn(subscribeOn)
}

fun <T> maybeOf(
    subscribeOn: Scheduler = Schedulers.io(),
    block: () -> T?
): Maybe<T> {
    return Maybe
        .create<T> { emitter ->
            val value = block()
            if (value != null) {
                emitter.onSuccess(value)
            } else {
                emitter.onError(NullPointerException("No value found..."))
            }
            emitter.onComplete()
        }
        .subscribeOn(subscribeOn)
}

fun List<Completable>.merge(): Completable = Completable.merge(this)

fun SharedPreferences.getIntOrNullIfDefault(key: String, default: Int): Int? {
    val value = getInt(key, default)
    return if (value != default) value else null
}

fun <T> List<T>.indexOfOrNull(value: T): Int? {
    return this.indexOf(value)
        .let { index ->
            if (index > -1) {
                index
            } else {
                null
            }
        }
}

fun Int.isLastIndexIn(list: List<*>): Boolean {
    return (this == list.size - 1)
}

@SuppressLint("RestrictedApi")
fun Fragment.registerForPopupMenu(
    anchor: View,
    @MenuRes menuRes: Int,
    onMenuItemListener: PopupMenu.OnMenuItemClickListener
) {

    val popupMenu = PopupMenu(requireContext(), anchor)

    popupMenu.menuInflater.inflate(menuRes, popupMenu.menu)
    popupMenu.setOnMenuItemClickListener(onMenuItemListener)

    val menuHelper = MenuPopupHelper(requireContext(), popupMenu.menu as MenuBuilder, anchor)
        .apply {
            setForceShowIcon(true)
        }

    anchor.setOnClickListener { menuHelper.show() }
}