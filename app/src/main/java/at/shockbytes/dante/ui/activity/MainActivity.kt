package at.shockbytes.dante.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.MenuItem
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.signin.DanteUser
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.adapter.BookPagerAdapter
import at.shockbytes.dante.ui.fragment.MenuFragment
import at.shockbytes.dante.ui.fragment.dialog.GoogleSignInDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.GoogleWelcomeScreenDialogFragment
import at.shockbytes.dante.ui.viewmodel.MainViewModel
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.flagging.FeatureFlagging
import at.shockbytes.dante.util.loadBitmap
import at.shockbytes.dante.util.toggle
import at.shockbytes.util.AppUtils
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity(), ViewPager.OnPageChangeListener {

    @Inject
    protected lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    protected lateinit var featureFlagging: FeatureFlagging

    protected var tabId: Int = R.id.menu_navigation_current

    private lateinit var pagerAdapter: BookPagerAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this, vmFactory)[MainViewModel::class.java]
        tabId = savedInstanceState?.getInt("tabId") ?: R.id.menu_navigation_current

        setupUI()
        initializeNavigation()
        setupObserver()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            DanteUtils.rcSignIn -> {
                data?.let { d ->
                    val onlineBackend = d.getBooleanExtra("onlineBackend", false)
                    viewModel.signIn(d, signInToBackend = onlineBackend)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt("tabId", tabId)
    }

    override fun onPageSelected(position: Int) {

        tabId = mainBottomNavigation.menu.getItem(position).itemId
        mainBottomNavigation.selectedItemId = tabId

        appBar.setExpanded(true, true)
        fab.toggle()
    }

    override fun onPageScrollStateChanged(state: Int) {}
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    // ---------------------------------------------------

    private fun setupObserver() {

        viewModel.userEvent.observe(this, Observer { event ->

            when (event) {

                is MainViewModel.UserEvent.SuccessEvent -> {

                    if (event.user != null) {
                        event.user.photoUrl?.let { photoUrl ->
                            photoUrl.loadBitmap(this).subscribe({ bm ->
                                imgButtonMainToolbarMore.setImageDrawable(AppUtils.createRoundedBitmap(this, bm))
                            }, { throwable: Throwable ->
                                throwable.printStackTrace()
                            })
                        }
                        showGoogleWelcomeScreen(event.user, event.showWelcomeScreen)
                    } else {
                        imgButtonMainToolbarMore.setImageResource(R.drawable.ic_overflow)
                    }
                }

                is MainViewModel.UserEvent.LoginEvent -> {
                    imgButtonMainToolbarMore.setImageResource(R.drawable.ic_overflow)

                    GoogleSignInDialogFragment.newInstance()
                            .setSignInListener { withOnlineBackend ->
                                startActivityForResult(event.signInIntent
                                        ?.putExtra("onlineBackend", withOnlineBackend), DanteUtils.rcSignIn)
                            }
                            .setMaybeLaterListener { viewModel.signInMaybeLater(true) }
                            .show(supportFragmentManager, "sign-in-fragment")
                }

                is MainViewModel.UserEvent.ErrorEvent -> {
                    showToast(getString(event.errorMsg))
                }
            }
        })

    }

    private fun setupUI() {

        fab.setOnClickListener {
            startActivity(BookRetrievalActivity.newIntent(this),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
        }

        imgButtonMainToolbarSearch.setOnClickListener {
            startActivity(SearchActivity.newIntent(this),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
        }

        imgButtonMainToolbarMore.setOnClickListener {
            MenuFragment.newInstance().show(supportFragmentManager, "menu-fragment")
        }
    }

    private fun initializeNavigation() {

        // Setup the ViewPager
        pagerAdapter = BookPagerAdapter(applicationContext, featureFlagging.showBookSuggestions,
                supportFragmentManager)
        viewPager.adapter = pagerAdapter
        viewPager.removeOnPageChangeListener(this) // Remove first to avoid multiple listeners
        viewPager.addOnPageChangeListener(this)
        viewPager.offscreenPageLimit = 2

        mainBottomNavigation.setOnNavigationItemSelectedListener { item ->
            colorNavigationItems(item)
            indexForNavigationItemId(item.itemId)?.let { viewPager.currentItem = it }
            true
        }
        mainBottomNavigation.menu.getItem(3).isVisible = featureFlagging.showBookSuggestions

        mainBottomNavigation.selectedItemId = tabId
    }

    private fun colorNavigationItems(item: MenuItem) {

        val stateListRes: Int = when (item.itemId) {
            R.id.menu_navigation_upcoming -> R.drawable.navigation_item_upcoming
            R.id.menu_navigation_current -> R.drawable.navigation_item_current
            R.id.menu_navigation_done -> R.drawable.navigation_item_done
            R.id.menu_navigation_suggestions -> R.drawable.navigation_item_suggestions
            else -> 0
        }

        val stateList = ContextCompat.getColorStateList(this, stateListRes)
        mainBottomNavigation.itemIconTintList = stateList
        mainBottomNavigation.itemTextColor = stateList
    }

    private fun showFabAwareSnackbar(text: String) {
        Snackbar.make(findViewById(R.id.main_content), text, Snackbar.LENGTH_SHORT).show()
    }

    private fun showGoogleWelcomeScreen(account: DanteUser, showWelcomeScreen: Boolean) {
        if (showWelcomeScreen) {
            GoogleWelcomeScreenDialogFragment
                    .newInstance(account.givenName, account.photoUrl)
                    .setOnAcknowledgedListener {
                        viewModel.showSignInWelcomeScreen(false)
                    }
                    .show(supportFragmentManager, "google_welcome_dialog_fragment")
        }
    }

    private fun indexForNavigationItemId(itemId: Int): Int? {
        return when (itemId) {
            R.id.menu_navigation_upcoming -> 0
            R.id.menu_navigation_current -> 1
            R.id.menu_navigation_done -> 2
            R.id.menu_navigation_suggestions -> 3
            else -> null
        }
    }

}