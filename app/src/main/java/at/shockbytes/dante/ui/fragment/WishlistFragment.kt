package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.injection.ViewModelFactory
import at.shockbytes.dante.ui.viewmodel.WishlistViewModel
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf
import kotlinx.android.synthetic.main.fragment_wishlist.*
import javax.inject.Inject

class WishlistFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_wishlist

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private val viewModel: WishlistViewModel by lazy { viewModelOf(vmFactory) }

    override fun setupViews() {
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {

        viewModel.requestWishlist()
        viewModel.getWishlist().observe(this, Observer(::handleWishlist))

    }

    private fun handleWishlist(state: WishlistViewModel.WishlistState) {
        when (state) {
            WishlistViewModel.WishlistState.Empty -> {
                tv_wishlist_empty.setVisible(true)
            }
            is WishlistViewModel.WishlistState.Present -> {
                tv_wishlist_empty.setVisible(false)
            }
        }
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance(): WishlistFragment {
            return WishlistFragment()
        }
    }
}
