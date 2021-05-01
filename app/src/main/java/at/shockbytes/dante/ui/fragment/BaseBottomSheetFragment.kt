package at.shockbytes.dante.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.viewbinding.ViewBinding
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable

abstract class BaseBottomSheetFragment<V : ViewBinding> : BottomSheetDialogFragment() {

    init {
        retainInstance = true
    }

    private var _binding: V? = null

    // This property is only valid between onCreateView and onDestroyView
    protected val vb: V
        get() = _binding!!

    val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun getTheme(): Int {
        return R.style.BottomSheetTheme
    }

    abstract fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): V

    abstract fun setupViews()

    protected abstract fun injectToGraph(appComponent: AppComponent)

    protected abstract fun bindViewModel()

    protected abstract fun unbindViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectToGraph((activity?.application as DanteApp).appComponent)
    }

    override fun onPause() {
        unbindViewModel()
        compositeDisposable.clear()
        super.onPause()
    }

    override fun onResume() {
        bindViewModel()
        super.onResume()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createViewBinding(inflater, container, false)
        return vb.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    fun showSnackBar(@StringRes messageRes: Int) {
        showSnackBar(getString(messageRes))
    }

    fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    @JvmOverloads
    protected fun showToast(text: String, showLong: Boolean = true) {
        val duration = if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(context, text, duration).show()
    }

    @JvmOverloads
    protected fun showToast(text: Int, showLong: Boolean = true) {
        showToast(getString(text), showLong)
    }
}