package at.shockbytes.dante.ui.activity

import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatDelegate
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import at.shockbytes.dante.R
import at.shockbytes.dante.camera.BarcodeScanResultBottomSheetDialogFragment
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.adapter.BookPagerAdapter
import at.shockbytes.dante.ui.fragment.MenuFragment
import at.shockbytes.dante.ui.fragment.dialog.QueryDialogFragment
import at.shockbytes.dante.ui.viewmodel.MainViewModel
import at.shockbytes.dante.core.image.GlideImageLoader.loadBitmap
import at.shockbytes.dante.ui.widget.DanteAppWidgetManager
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.ui.fragment.AnnouncementFragment
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.createRoundedBitmap
import at.shockbytes.dante.util.isFragmentShown
import at.shockbytes.dante.util.runDelayed
import at.shockbytes.dante.util.settings.ThemeState
import at.shockbytes.dante.util.toggle
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.tracking.properties.LoginSource
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class MainActivity : BaseActivity(), ViewPager.OnPageChangeListener {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var danteSettings: DanteSettings

    private var tabId: Int = R.id.menu_navigation_current

    private lateinit var pagerAdapter: BookPagerAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbarMain)

        viewModel = viewModelOf(vmFactory)
        tabId = savedInstanceState?.getInt(ID_SELECTED_TAB) ?: R.id.menu_navigation_current

        handleIntentExtras()
        setupUI()
        initializeNavigation()
        setupDarkMode()
        setupFabMorph()
    }

    private fun setupFabMorph() {
        mainFab.setOnClickListener {
            mainFab.isExpanded = !mainFab.isExpanded
        }
        dial_back.setOnClickListener {
            mainFab.isExpanded = !mainFab.isExpanded
        }
        dial_btn_manual.setOnClickListener {
            dial_back.callOnClick()
            // For whatever reason, this transition needs to take place
            // slightly later to not mess up the FAB morph transformation
            runDelayed(350) {
                navigateToManualAdd()
            }
        }
        dial_btn_scan.setOnClickListener {
            dial_back.callOnClick()
            runDelayed(300) {
                navigateToCamera()
            }
        }
        dial_btn_search_by_title.setOnClickListener {
            dial_back.callOnClick()
            runDelayed(300) {
                showAddByTitleDialog()
            }
        }
    }

    private fun animateActionBarItems() {
        runDelayed(300) {
            animateTitle()
            animateSearchIcon()
        }
    }

    private fun animateTitle() {
        txtMainToolbarTitle.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(500L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun animateSearchIcon() {
        imgButtonMainToolbarSearch.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(500L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            DanteUtils.RC_SIGN_IN -> data?.let(viewModel::signIn)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ID_SELECTED_TAB, tabId)
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
        mainFab.toggle()
    }

    override fun onPageScrollStateChanged(state: Int) = Unit
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

    // ---------------------------------------------------

    private fun bindViewModel() {
        viewModel.showAnnouncement()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::showAnnouncementFragment, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)

        viewModel.getUserEvent().observe(this, Observer(::handleUserEvent))

        viewModel.requestSeasonalTheme()
        viewModel.getSeasonalTheme()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(seasonalThemeView::setSeasonalTheme)
            .addTo(compositeDisposable)
    }

    private fun handleUserEvent(event: MainViewModel.UserEvent) {
        when (event) {

            is MainViewModel.UserEvent.LoggedIn -> {
                // Only show onboarding hints after the user login state is resolved
                checkForOnboardingHints()

                val photoUrl = event.user.photoUrl
                if (photoUrl != null) {
                    loadUserImage(photoUrl, onLoaded = ::onUserLoaded)
                } else {
                    onUserLoaded()
                }
            }

            is MainViewModel.UserEvent.AnonymousUser -> {
                imgButtonMainToolbarMore.setImageResource(R.drawable.ic_overflow)
                onUserLoaded()
            }
            // These cases are handled by another Fragment
            is MainViewModel.UserEvent.RequireLogin -> Unit
            is MainViewModel.UserEvent.Error -> Unit
        }
    }

    private fun loadUserImage(photoUrl: Uri, onLoaded: () -> Unit) {

        photoUrl.loadBitmap(this)
            .doFinally {
                onLoaded()
            }
            .map { bitmap ->
                createRoundedBitmap(bitmap)
            }
            .subscribe(imgButtonMainToolbarMore::setImageDrawable, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun onUserLoaded() {
        animateActionBarItems()
        viewModel.queryAnnouncements()
    }

    fun forceLogin(source: LoginSource) {
        viewModel.forceLogin(source)
    }

    private fun showAnnouncementFragment(unused: Unit) {
        with(supportFragmentManager) {
            if (!isFragmentShown(TAG_ANNOUNCEMENT)) {
                beginTransaction()
                    .setCustomAnimations(0, R.anim.fade_out, 0, R.anim.fade_out)
                    .add(android.R.id.content, AnnouncementFragment.newInstance(), TAG_ANNOUNCEMENT)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun handleIntentExtras() {

        val bookDetailInfo = intent.getParcelableExtra<Destination.BookDetail.BookDetailInfo>(ARG_OPEN_BOOK_DETAIL_FOR_ID)
        val openCameraAfterLaunch = intent.getBooleanExtra(ARG_OPEN_CAMERA_AFTER_LAUNCH, false)
        val hasAppShortcutExtra = intent.hasExtra("app_shortcut")

        when {
            bookDetailInfo != null -> navigateToBookDetailScreen(bookDetailInfo)
            openCameraAfterLaunch -> showToast(R.string.open_camera)
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

    private fun checkForOnboardingHints() {

        // It has to be delayed, otherwise it will appear on the wrong
        // position on top of the BottomNavigationBar
        runDelayed(1000) {
            if (danteSettings.isFirstAppOpen) {
                danteSettings.isFirstAppOpen = false
                showOnboardingHintViews()
            }
        }
    }

    private fun showOnboardingHintViews() {
        MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.mainFab)
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
        pagerAdapter = BookPagerAdapter(applicationContext, supportFragmentManager)
        viewPager.apply {
            adapter = pagerAdapter
            removeOnPageChangeListener(this@MainActivity) // Remove first to avoid multiple listeners
            addOnPageChangeListener(this@MainActivity)
            offscreenPageLimit = 2
        }

        mainBottomNavigation.apply {
            setOnNavigationItemSelectedListener { item ->
                colorNavigationItems(item)
                indexForNavigationItemId(item.itemId)?.let { viewPager.currentItem = it }
                true
            }
            selectedItemId = tabId
        }
    }

    private fun colorNavigationItems(item: MenuItem) {

        val stateListRes: Int = when (item.itemId) {
            R.id.menu_navigation_upcoming -> R.drawable.navigation_item_upcoming
            R.id.menu_navigation_current -> R.drawable.navigation_item_current
            R.id.menu_navigation_done -> R.drawable.navigation_item_done
            else -> 0
        }

        val stateList = ContextCompat.getColorStateList(this, stateListRes)
        mainBottomNavigation.itemIconTintList = stateList
        mainBottomNavigation.itemTextColor = stateList
    }

    private fun indexForNavigationItemId(itemId: Int): Int? {
        return when (itemId) {
            R.id.menu_navigation_upcoming -> 0
            R.id.menu_navigation_current -> 1
            R.id.menu_navigation_done -> 2
            else -> null
        }
    }

    private fun navigateToCamera() {
        ActivityNavigator.navigateTo(
            this,
            Destination.BarcodeScanner,
            ActivityOptionsCompat
                .makeClipRevealAnimation(
                    mainFab,
                    mainFab.x.toInt(),
                    mainFab.y.toInt(),
                    mainFab.width,
                    mainFab.height
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

        private const val ID_SELECTED_TAB = "selected_tab_id"
        private const val TAG_ANNOUNCEMENT = "announcement-tag"

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