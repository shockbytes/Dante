package at.shockbytes.dante.ui.activity.core

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

abstract class BaseBindingActivity<V : ViewBinding> : BaseActivity() {

    protected lateinit var vb: V

    fun setContentViewWithBinding(creator: ((layoutInflater: LayoutInflater) -> V)? = null) {
        creator?.run {
            vb = this(layoutInflater)
            setContentView(vb.root)
        }
    }
}