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
import at.shockbytes.dante.ui.viewmodel.MainViewModel
import at.shockbytes.dante.core.image.GlideImageLoader.loadBitmap
import at.shockbytes.dante.ui.widget.DanteAppWidgetManager
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.core.shortcut.AppShortcutHandler
import at.shockbytes.dante.ui.fragment.AnnouncementFragment
import at.shockbytes.dante.ui.viewmodel.UserViewModel
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.createRoundedBitmap
import at.shockbytes.dante.util.isFragmentShown
import at.shockbytes.dante.util.runDelayed
import at.shockbytes.dante.util.settings.ThemeState
import at.shockbytes.dante.util.toggle
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.tracking.properties.LoginSource
import at.shockbytes.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class MainActivity : BaseActivity(), ViewPager.OnPageChangeListener {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var danteSettings: DanteSettings
    
    @Inject
    lateinit var appShortcutHandler: AppShortcutHandler

    private var tabId: Int = R.id.menu_navigation_current

    private lateinit var pagerAdapter: BookPagerAdapter

    private lateinit var viewModel: MainViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbarMain)

        viewModel = viewModelOf(vmFactory)
        userViewModel = viewModelOf(vmFactory)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ID_SELECTED_TAB, tabId)
    }

    override fun onStart() {
        super.onStart()
        appShortcutHandler.handleAppShortcutForActivity(
            activity = this,
            shortcutTitle = "extra_app_shortcut_title",
            action = ::showAddByTitleDialog
        )
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
        viewModel.onMainEvent()
            .filter { event -> event is MainViewModel.MainEvent.Announcement }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::showAnnouncementFragment, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)

        userViewModel.getUserViewState().observe(this, Observer(::handleUserViewState))

        viewModel.requestSeasonalTheme()
        viewModel.getSeasonalTheme()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(seasonalThemeView::setSeasonalTheme)
            .addTo(compositeDisposable)
    }

    private fun handleUserViewState(userViewState: UserViewModel.UserViewState) {
        when (userViewState) {

            is UserViewModel.UserViewState.LoggedIn -> {
                // Only show onboarding hints after the user login state is resolved
                checkForOnboardingHints()

                val photoUrl = userViewState.user.photoUrl
                if (photoUrl != null) {
                    loadUserImage(photoUrl, onLoaded = ::onUserLoaded)
                } else {
                    onUserLoaded()
                }
            }

            is UserViewModel.UserViewState.UnauthenticatedUser -> {
                imgButtonMainToolbarMore.setImageResource(R.drawable.ic_overflow)
                onUserLoaded()
            }
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
        userViewModel.forceLogin(source)
    }

    private fun showAnnouncementFragment(unused: MainViewModel.MainEvent) {
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

        when {
            bookDetailInfo != null -> navigateToBookDetailScreen(bookDetailInfo)
            openCameraAfterLaunch -> showToast(R.string.open_camera)
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
        runDelayed(3_000) {
            if (danteSettings.hasUserSeenOnboardingHints) {
                danteSettings.hasUserSeenOnboardingHints = false
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
        MaterialDialog(this).show {
            icon(R.drawable.ic_search)
            title(R.string.dialogfragment_query_title)
            message(R.string.dialogfragment_query_message)
            input(allowEmpty = false, hintRes = R.string.manual_query) { _, query ->
                // Remove blanks with + so query works also for titles
                val correctedQuery = query.toString().replace(' ', '+')
                BarcodeScanResultBottomSheetDialogFragment
                    .newInstance(correctedQuery, askForAnotherScan = false)
                    .show(supportFragmentManager, "show-bottom-sheet-with-book")
            }
            positiveButton(android.R.string.search_go)
            cancelOnTouchOutside(true)
            cornerRadius(AppUtils.convertDpInPixel(6, this@MainActivity).toFloat())
        }
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