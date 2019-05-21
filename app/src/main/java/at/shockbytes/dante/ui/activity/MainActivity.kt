package at.shockbytes.dante.ui.activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.menu.MenuBuilder
import android.view.MenuItem
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
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
import at.shockbytes.dante.ui.image.GlideImageLoader.loadBitmap
import at.shockbytes.dante.ui.widget.DanteAppWidgetManager
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.flagging.FeatureFlag
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.util.toggleVisibility
import at.shockbytes.util.AppUtils
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class MainActivity : BaseActivity(), androidx.viewpager.widget.ViewPager.OnPageChangeListener {

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

        viewModel = ViewModelProviders.of(this, vmFactory)[MainViewModel::class.java]
        tabId = savedInstanceState?.getInt("tabId") ?: R.id.menu_navigation_current

        handleIntentExtras()
        setupUI()
        initializeNavigation()
        bindViewModel()
        setupDarkMode()
        checkForOnboardingHints()
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

    override fun onDestroy() {
        super.onDestroy()
        DanteAppWidgetManager.refresh(this)
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
            mainFabMenu.addActionItem(SpeedDialActionItem.Builder(item.itemId, item.icon)
                    .setLabel(item.title.toString())
                    .setFabBackgroundColor(ContextCompat.getColor(this, bgColors[idx]))
                    .create())
        }

        mainFabMenu.setOnActionSelectedListener { item ->

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

    private fun navigateToCamera() {
        ActivityNavigator.navigateTo(
            this,
            Destination.Retrieval(BookRetrievalActivity.RetrievalType.CAMERA, null),
            ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    private fun navigateToManualAdd() {
        ActivityNavigator.navigateTo(
            this,
            Destination.ManualAdd,
            ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    private fun showAddByTitleDialog() {
        QueryDialogFragment.newInstance()
            .setOnQueryEnteredListener { query ->

                ActivityNavigator.navigateTo(
                    this,
                    Destination.Retrieval(BookRetrievalActivity.RetrievalType.TITLE, query),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
                )
            }
            .show(supportFragmentManager, "query-dialog-fragment")
    }

    private fun setupDarkMode() {
        enableDarkMode(danteSettings.darkModeEnabled)

        danteSettings
            .observeDarkModeEnabled()
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

    private fun checkForAppShortcut() {
        handleAppShortcut(intent.getStringExtra("app_shortcut"))
    }

    private fun handleAppShortcut(stringExtra: String) {

        when (stringExtra) {
            "extra_app_shortcut_title" -> showAddByTitleDialog()
        }
    }

    companion object {

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