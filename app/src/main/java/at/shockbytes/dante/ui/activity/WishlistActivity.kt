package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.databinding.ActivityWishlistBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.BaseBindingActivity
import at.shockbytes.dante.ui.fragment.MainBookFragment
import at.shockbytes.dante.util.setVisible

class WishlistActivity : BaseBindingActivity<ActivityWishlistBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithBinding(ActivityWishlistBinding::inflate)
        supportActionBar?.hide()

        setupToolbar()

        supportFragmentManager.beginTransaction()
            .replace(
                vb.wishlistFragmentPlaceholder.id,
                MainBookFragment.newInstance(BookState.WISHLIST)
            )
            .commit()
    }

    private fun setupToolbar() {
        with(vb.toolbarWishlist) {
            danteToolbarTitle.setText(R.string.wishlist_title)
            danteToolbarBack.apply {
                setVisible(true)
                setOnClickListener {
                    onBackPressed()
                }
            }
        }
    }


    override fun injectToGraph(appComponent: AppComponent) = Unit

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, WishlistActivity::class.java)
    }
}