package kotterknife

import android.app.Activity
import android.app.Dialog
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import androidx.recyclerview.widget.RecyclerView

fun <V : View> View.bindView(id: Int): ReadOnlyProperty<View, V> = required(id, viewFinder)

fun <V : View> Activity.bindView(id: Int): ReadOnlyProperty<Activity, V> = required(id, viewFinder)

fun <V : View> Dialog.bindView(id: Int): ReadOnlyProperty<Dialog, V> = required(id, viewFinder)

fun <V : View> DialogFragment.bindView(id: Int): ReadOnlyProperty<DialogFragment, V> = required(id, viewFinder)

fun <V : View> Fragment.bindView(id: Int): ReadOnlyProperty<Fragment, V> = required(id, viewFinder)

fun <V : View> RecyclerView.ViewHolder.bindView(id: Int): ReadOnlyProperty<RecyclerView.ViewHolder, V> = required(id, viewFinder)

private val View.viewFinder: Finder<View>
    get() = { findViewById(it) }
private val Activity.viewFinder: Finder<Activity>
    get() = { findViewById(it) }
private val Dialog.viewFinder: Finder<Dialog>
    get() = { findViewById(it) }
private val DialogFragment.viewFinder: Finder<DialogFragment>
    get() = { dialog?.findViewById(it) ?: view?.findViewById(it) }
private val Fragment.viewFinder: Finder<Fragment>
    get() = { view!!.findViewById(it) }
private val RecyclerView.ViewHolder.viewFinder: Finder<RecyclerView.ViewHolder>
    get() = { itemView.findViewById(it) }

private fun viewNotFound(id: Int, desc: KProperty<*>): Nothing =
    throw IllegalStateException("View ID $id for '${desc.name}' not found.")

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(id: Int, finder: Finder<T>) = Lazy { t: T, desc ->
    t.finder(id) as V? ?: viewNotFound(id, desc)
}

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> optional(id: Int, finder: Finder<T>) = Lazy { t: T, _ -> t.finder(id) as V? }

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(ids: IntArray, finder: Finder<T>) = Lazy { t: T, desc ->
    ids.map {
        t.finder(it) as V? ?: viewNotFound(it, desc)
    }
}

typealias Finder<T> = T.(Int) -> View?

// Like Kotlin's lazy delegate but the initializer gets the target and metadata passed to it
private class Lazy<T, V>(private val initializer: (T, KProperty<*>) -> V) : ReadOnlyProperty<T, V> {
    private object EMPTY

    private var value: Any? = EMPTY

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        if (value == EMPTY) {
            value = initializer(thisRef, property)
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }
}