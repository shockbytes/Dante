package at.shockbytes.dante.ui.activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.menu.MenuBuilder
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.viewpager.widget.ViewPager
import at.shockbytes.dante.R
import at.shockbytes.dante.camera.BarcodeScanResultBottomSheetDialogFragment
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.signin.DanteUser
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.adapter.BookPagerAdapter
import at.shockbytes.dante.ui.fragment.MenuFragment
import at.shockbytes.dante.ui.fragment.dialog.GoogleSignInDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.GoogleWelcomeScreenDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.QueryDialogFragment
import at.shockbytes.dante.ui.viewmodel.MainViewModel
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.flagging.FeatureFlagging
import at.shockbytes.dante.core.image.GlideImageLoader.loadBitmap
import at.shockbytes.dante.ui.widget.DanteAppWidgetManager
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.flagging.FeatureFlag
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.ui.fragment.AnnouncementFragment
import at.shockbytes.dante.util.retrieveActiveActivityAlias
import at.shockbytes.dante.util.settings.LauncherIconState
import at.shockbytes.dante.util.settings.ThemeState
import at.shockbytes.dante.util.toggleVisibility
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.AppUtils
import com.leinardi.android.speeddial.SpeedDialActionItem
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class MainActivity : BaseActivity(), ViewPager.OnPageChangeListener {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var featureFlagging: FeatureFlagging

    @Inject
    lateinit var danteSettings: DanteSettings

    private var tabId: Int = R.id.menu_navigation_current

    private lateinit var pagerAdapter: BookPagerAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = viewModelOf(vmFactory)
        tabId = savedInstanceState?.getInt("tabId") ?: R.id.menu_navigation_current

        handleIntentExtras()
        setupUI()
        initializeNavigation()
        setupDarkMode()
        checkForOnboardingHints()
        saveLauncherIconState()
        // goingEdgeToEdge()
    }

    private fun goingEdgeToEdge() {
        window.decorView.systemUiVisibility =
            // Tells the system that the window wishes the content to
            // be laid out at the most extreme scenario. See the docs for
            // more information on the specifics
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                // Tells the system that the window wishes the content to
                // be laid out as if the navigation bar was hidden
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    private fun animateTitle() {
        txtMainToolbarTitle.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            DanteUtils.rcSignIn -> data?.let(viewModel::signIn)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("tabId", tabId)
    }

    override fun onStart() {
        super.onStart()
        setupFabMenu()
    }

    override fun onStop() {
        super.onStop()
        DanteAppWidgetManager.refresh(this)
    }

    override fun onResume() {
        super.onResume()
        bindViewModel()
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

        viewModel.hasActiveAnnouncement()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ hasAnnouncement ->
                if (hasAnnouncement) {
                    showAnnouncementFragment()
                }
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)

        viewModel.getUserEvent().observe(this, Observer { event ->

            when (event) {

                is MainViewModel.UserEvent.SuccessEvent -> {
                    // Only show announcements once the user is logged in
                    viewModel.queryAnnouncements()

                    if (event.user != null) {
                        val photoUrl = event.user.photoUrl
                        if (photoUrl != null) {
                            loadImageUrl(photoUrl)
                        } else {
                            animateTitle()
                        }
                        showGoogleWelcomeScreen(event.user, event.showWelcomeScreen)
                    } else {
                        imgButtonMainToolbarMore.setImageResource(R.drawable.ic_overflow)
                        animateTitle()
                    }
                }

                is MainViewModel.UserEvent.LoginEvent -> {
                    imgButtonMainToolbarMore.setImageResource(R.drawable.ic_overflow)

                    GoogleSignInDialogFragment.newInstance()
                        .setSignInListener {
                            startActivityForResult(event.signInIntent, DanteUtils.rcSignIn)
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

    private fun loadImageUrl(photoUrl: Uri) {

        photoUrl
            .loadBitmap(this)
            .doFinally {
                animateTitle()
            }
            .subscribe({ bm ->
                imgButtonMainToolbarMore.setImageDrawable(AppUtils.createRoundedBitmap(this, bm))
            }, { throwable: Throwable ->
                throwable.printStackTrace()
            })
            .addTo(compositeDisposable)
    }

    private fun showAnnouncementFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, R.anim.fade_out, 0, R.anim.fade_out)
            .add(android.R.id.content, AnnouncementFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    private fun handleIntentExtras() {

        val bookDetailInfo: Destination.BookDetail.BookDetailInfo? = intent.getParcelableExtra(ARG_OPEN_BOOK_DETAIL_FOR_ID)
        val openCameraAfterLaunch = intent.getBooleanExtra(ARG_OPEN_CAMERA_AFTER_LAUNCH, false)
        val hasAppShortcutExtra = intent.hasExtra("app_shortcut")

        when {
            bookDetailInfo != null -> navigateToBookDetailScreen(bookDetailInfo)
            openCameraAfterLaunch -> showToast("Open camera right now...")
            hasAppShortcutExtra -> checkForAppShortcut()
        }
    }

    private fun navigateToBookDetailScreen(bookDetailInfo: Destination.BookDetail.BookDetailInfo) {
        ActivityNavigator.navigateTo(this, Destination.BookDetail(bookDetailInfo))
    }

    private fun setupUI() {
        imgButtonMainToolbarSearch.setOnClickListener {
            ActivityNavigator.navigateTo(
                this,
                Destination.Search,
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
            mainFabMenu.addActionItem(
                SpeedDialActionItem.Builder(item.itemId, item.icon)
                    .setLabel(item.title.toString())
                    .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                    .setFabBackgroundColor(ContextCompat.getColor(this, bgColors[idx]))
                    .create()
            )
        }

        mainFabMenu.setOnActionSelectedListener { item ->
            mainFabMenuOverlay.hide(false)

            when (item.id) {

                R.id.menu_fab_add_camera -> {
                    navigateToCamera()
                    false
                }
                R.id.menu_fab_add_title -> {
                    showAddByTitleDialog()
                    false
                }
                R.id.menu_fab_add_manually -> {
                    navigateToManualAdd()
                    false
                }
                else -> true
            }
        }
    }

    private fun checkForOnboardingHints() {

        // It has to be delayed, otherwise it will appear on the wrong
        // position on top of the BottomNavigationBar
        Handler().postDelayed({
            if (danteSettings.isFirstAppOpen) {
                danteSettings.isFirstAppOpen = false
                showOnboardingHintViews()
            }
        }, 1500)
    }

    private fun saveLauncherIconState() {
        val aliasName = retrieveActiveActivityAlias()
        val state = LauncherIconState.ofStringOrDefault(aliasName)
        danteSettings.selectedLauncherIconState = state
    }

    private fun showOnboardingHintViews() {

        MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.mainFabMenu)
            .setFocalColour(ContextCompat.getColor(this, android.R.color.transparent))
            .setPrimaryTextColour(ContextCompat.getColor(this, R.color.colorPrimaryTextLight))
            .setSecondaryTextColour(ContextCompat.getColor(this, R.color.colorSecondaryTextLight))
            .setBackgroundColour(ContextCompat.getColor(this, R.color.iconColorSettings))
            .setPrimaryText(R.string.fab_hint_prompt)
            .setSecondaryText(R.string.fab_hint_prompt_message)
            .show()
    }

    private fun initializeNavigation() {

        // Setup the ViewPager
        pagerAdapter = BookPagerAdapter(applicationContext, featureFlagging[FeatureFlag.BOOK_SUGGESTIONS],
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
        mainBottomNavigation.menu.getItem(3).isVisible = featureFlagging[FeatureFlag.BOOK_SUGGESTIONS]
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

    private fun showGoogleWelcomeScreen(account: DanteUser, showWelcomeScreen: Boolean) {
        if (showWelcomeScreen && supportFragmentManager.findFragmentByTag(GOOGLE_SIGNIN_FRAGMENT) == null) {
            GoogleWelcomeScreenDialogFragment
                .newInstance(account.givenName, account.photoUrl)
                .setOnAcknowledgedListener {
                    viewModel.showSignInWelcomeScreen(false)
                }
                .show(supportFragmentManager, GOOGLE_SIGNIN_FRAGMENT)
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

    private fun navigateToCamera() {
        ActivityNavigator.navigateTo(
            this,
            Destination.BarcodeScanner,
            ActivityOptionsCompat
                .makeClipRevealAnimation(
                    mainFabMenu,
                    mainFabMenu.x.toInt(),
                    mainFabMenu.y.toInt(),
                    mainFabMenu.width,
                    mainFabMenu.height
                )
                .toBundle()
        )
    }

    private fun navigateToManualAdd() {
        ActivityNavigator.navigateTo(
            this,
            Destination.ManualAdd(),
            ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    private fun showAddByTitleDialog() {
        QueryDialogFragment.newInstance()
            .setOnQueryEnteredListener { query ->
                BarcodeScanResultBottomSheetDialogFragment
                    .newInstance(query, askForAnotherScan = false)
                    .show(supportFragmentManager, "show-bottom-sheet-with-book")
            }
            .show(supportFragmentManager, "query-dialog-fragment")
    }

    private fun setupDarkMode() {
        setupTheme(danteSettings.themeState)

        danteSettings
            .observeThemeChanged()
            .subscribe(::setupTheme)
            .addTo(compositeDisposable)
    }

    private fun setupTheme(theme: ThemeState) {
        AppCompatDelegate.setDefaultNightMode(theme.themeMode)
    }

    private fun checkForAppShortcut() {
        handleAppShortcut(intent.getStringExtra("app_shortcut"))
    }

    private fun handleAppShortcut(stringExtra: String?) {

        when (stringExtra) {
            "extra_app_shortcut_title" -> showAddByTitleDialog()
        }
    }

    companion object {

        private const val GOOGLE_SIGNIN_FRAGMENT = "google_welcome_dialog_fragment"

        private const val ARG_OPEN_CAMERA_AFTER_LAUNCH = "arg_open_camera_after_lunch"
        private const val ARG_OPEN_BOOK_DETAIL_FOR_ID = "arg_open_book_detail_for_id"

        fun newIntent(
            context: Context,
            bookDetailInfo: Destination.BookDetail.BookDetailInfo? = null,
            openCameraAfterLaunch: Boolean = false
        ): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra(ARG_OPEN_BOOK_DETAIL_FOR_ID, bookDetailInfo)
                .putExtra(ARG_OPEN_CAMERA_AFTER_LAUNCH, openCameraAfterLaunch)
        }
    }
}