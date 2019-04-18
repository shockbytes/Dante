package at.shockbytes.dante.ui.activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.menu.MenuBuilder
import android.view.MenuItem
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.signin.DanteUser
import at.shockbytes.dante.ui.activity.core.ActivityNavigation
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.adapter.BookPagerAdapter
import at.shockbytes.dante.ui.fragment.MenuFragment
import at.shockbytes.dante.ui.fragment.dialog.GoogleSignInDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.GoogleWelcomeScreenDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.QueryDialogFragment
import at.shockbytes.dante.ui.viewmodel.MainViewModel
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.flagging.FeatureFlagging
import at.shockbytes.dante.ui.image.GlideImageLoader.loadBitmap
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.flagging.FeatureFlag
import at.shockbytes.dante.util.toggleVisibility
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.dante.util.tracking.event.DanteTrackingEvent
import at.shockbytes.util.AppUtils
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : BaseActivity(), androidx.viewpager.widget.ViewPager.OnPageChangeListener {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var featureFlagging: FeatureFlagging

    @Inject
    lateinit var danteSettings: DanteSettings

    @Inject
    lateinit var tracker: Tracker

    private var tabId: Int = R.id.menu_navigation_current

    private lateinit var pagerAdapter: BookPagerAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this, vmFactory)[MainViewModel::class.java]
        tabId = savedInstanceState?.getInt("tabId") ?: R.id.menu_navigation_current

        intent.getBooleanExtra(ARG_OPEN_CAMERA_AFTER_LAUNCH, false).let { openCam ->
            Timber.d("Should open camera: $openCam")
        }

        setupUI()
        initializeNavigation()
        bindViewModel()
        setupDarkMode()
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

    override fun onStart() {
        super.onStart()
        setupFabMenu()
    }

    override fun onPageSelected(position: Int) {

        tabId = mainBottomNavigation.menu.getItem(position).itemId
        mainBottomNavigation.selectedItemId = tabId

        appBar.setExpanded(true, true)
        mainFabMenu.toggleVisibility()
    }

    override fun onPageScrollStateChanged(state: Int) = Unit
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

    // ---------------------------------------------------

    private fun bindViewModel() {
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
        imgButtonMainToolbarSearch.setOnClickListener {
            ActivityNavigation.navigateTo(
                    this,
                    ActivityNavigation.Destination.Search,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
            )
        }
        imgButtonMainToolbarMore.setOnClickListener {
            MenuFragment.newInstance().show(supportFragmentManager, "menu-fragment")
        }
    }

    private fun setupFabMenu() {

        val menu = MenuBuilder(this)
        menuInflater.inflate(R.menu.menu_fab, menu)

        val bgColors = listOf(R.color.tabcolor_upcoming, R.color.tabcolor_done, R.color.color_error)
        menu.visibleItems.forEachIndexed { idx, item ->
            mainFabMenu.addActionItem(SpeedDialActionItem.Builder(item.itemId, item.icon)
                    .setLabel(item.title.toString())
                    .setFabBackgroundColor(ContextCompat.getColor(this, bgColors[idx]))
                    .create())
        }

        mainFabMenu.setOnActionSelectedListener { item ->

            when (item.id) {

                R.id.menu_fab_add_camera -> {
                    tracker.trackEvent(DanteTrackingEvent.OpenCameraEvent())

                    ActivityNavigation.navigateTo(
                            this,
                            ActivityNavigation.Destination.Retrieval(BookRetrievalActivity.RetrievalType.CAMERA, null),
                            ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
                    )
                    false
                }
                R.id.menu_fab_add_title -> {
                    QueryDialogFragment.newInstance()
                            .setOnQueryEnteredListener { query ->
                                tracker.trackEvent(DanteTrackingEvent.OpenTitleSearchEvent())

                                ActivityNavigation.navigateTo(
                                        this,
                                        ActivityNavigation.Destination.Retrieval(BookRetrievalActivity.RetrievalType.TITLE, query),
                                        ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
                                )
                            }
                            .show(supportFragmentManager, "query-dialog-fragment")
                    false
                }
                R.id.menu_fab_add_manually -> {
                    tracker.trackEvent(DanteTrackingEvent.OpenManualAddViewEvent())

                    ActivityNavigation.navigateTo(
                            this,
                            ActivityNavigation.Destination.ManualAdd,
                            ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
                    )
                    false
                }
                else -> true
            }
        }
    }

    private fun initializeNavigation() {

        // Setup the ViewPager
        pagerAdapter = BookPagerAdapter(applicationContext, featureFlagging[FeatureFlag.BookSuggestions],
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
        mainBottomNavigation.menu.getItem(3).isVisible = featureFlagging[FeatureFlag.BookSuggestions]
        mainBottomNavigation.selectedItemId = tabId
    }

    private fun colorNavigationItems(item: MenuItem) {

        val stateListRes: Int = when (item.itemId) {
            R.id.menu_navigation_upcoming -> R.drawable.navigation_item
            R.id.menu_navigation_current -> R.drawable.navigation_item_current
            R.id.menu_navigation_done -> R.drawable.navigation_item_done
            R.id.menu_navigation_suggestions -> R.drawable.navigation_item_suggestions
            else -> 0
        }

        val stateList = ContextCompat.getColorStateList(this, stateListRes)
        mainBottomNavigation.itemIconTintList = stateList
        mainBottomNavigation.itemTextColor = stateList
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

    private fun setupDarkMode() {
        enableDarkMode(danteSettings.darkModeEnabled)
        danteSettings.observeDarkModeEnabled()
                .subscribe { isDarkModeEnabled ->
                    enableDarkMode(isDarkModeEnabled)
                }
                .addTo(compositeDisposable)
    }

    private fun enableDarkMode(isEnabled: Boolean) {
        val mode = if (isEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    companion object {

        private const val ARG_OPEN_CAMERA_AFTER_LAUNCH = "arg_open_camera_after_lunch"

        fun newIntent(context: Context, openCameraAfterLaunch: Boolean): Intent {
            return Intent(context, MainActivity::class.java)
                    .putExtra(ARG_OPEN_CAMERA_AFTER_LAUNCH, openCameraAfterLaunch)
        }
    }
}