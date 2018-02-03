package at.shockbytes.dante.ui.activity

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import at.shockbytes.dante.R
import at.shockbytes.dante.adapter.BookAdapter
import at.shockbytes.dante.adapter.BookPagerAdapter
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.signin.GoogleSignInManager
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.fragment.dialog.GoogleSignInDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.GoogleWelcomeScreenDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.StatsDialogFragment
import at.shockbytes.dante.util.AppParams
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.util.AppUtils
import butterknife.OnClick
import com.crashlytics.android.Crashlytics
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import icepick.Icepick
import icepick.State
import kotterknife.bindView
import javax.inject.Inject

class MainActivity : BaseActivity(), BookAdapter.OnBookPopupItemSelectedListener,
        ViewPager.OnPageChangeListener {

    private val tabLayout: TabLayout by bindView(R.id.tablayout)
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val appBar: AppBarLayout by bindView(R.id.appbar)
    private val fab: FloatingActionButton by bindView(R.id.main_fab)
    private val viewpager: ViewPager by bindView(R.id.viewpager)

    @Inject
    protected lateinit var bookManager: BookManager

    @Inject
    protected lateinit var backupManager: BackupManager

    @Inject
    protected lateinit var signInManager: GoogleSignInManager

    @Inject
    protected lateinit var tracker: Tracker

    @State
    @JvmField
    protected var primaryOld: Int = 0

    @State
    @JvmField
    protected var primaryDarkOld: Int = 0

    @State
    @JvmField
    protected var tabPosition: Int = 0

    private lateinit var pagerAdapter: BookPagerAdapter

    private var menuItemBackup: MenuItem? = null
    private var menuItemGoogle: MenuItem? = null
    private var menuItemLogin: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupIcepick(savedInstanceState)
        setupGoogleServices()
        initializeTabs()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onDestroy() {
        backupManager.close()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            AppParams.rcScanBook -> {
                if (resultCode == RESULT_OK) {
                    val bookId = data.getLongExtra(AppParams.extraBookId, -1)
                    if (bookId > -1) {
                        pagerAdapter.listener?.onBookAdded(bookManager.getBook(bookId))
                    }
                }
            }
            GoogleSignInManager.rcSignIn -> {
                signInManager.signIn(data).subscribe({ account ->
                    if (account != null) {
                        showFabAwareSnackbar(getString(R.string.signin_welcome, account.givenName))
                        connectToGoogleServices()
                        showGoogleWelcomeScreen(account)
                    } else {
                        showFabAwareSnackbar(getString(R.string.signin_no_account))
                    }
                }, { throwable: Throwable ->
                    throwable.printStackTrace()
                    Crashlytics.logException(throwable)
                    showFabAwareSnackbar(getString(R.string.error_google_login))
                })
            }
            BackupActivity.rcBackupRestored -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Backup is restored, data no longer valid --> refresh
                    initializeTabs()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
        val id = item.itemId
        when (id) {
            R.id.action_settings -> {
                startActivity(SettingsActivity.newIntent(applicationContext), options)
            }
            R.id.action_stats -> {
                val fragment = StatsDialogFragment.newInstance()
                fragment.show(supportFragmentManager, "stats-dialog-fragment")
            }
            R.id.action_backup -> {
                startActivityForResult(BackupActivity.newIntent(this),
                        BackupActivity.rcBackupRestored, options)
            }
            R.id.action_google_login -> {

                signInManager.isSignedIn(this).subscribe { isSignedIn ->

                    if (isSignedIn) {
                        signInManager.signOut().subscribe({
                            tryPersonalizeMenu()
                            showFabAwareSnackbar(getString(R.string.signin_goodbye))
                        }, { throwable: Throwable ->
                            throwable.printStackTrace()
                            Crashlytics.logException(throwable)
                            showFabAwareSnackbar(getString(R.string.error_google_logout))
                        })
                    } else {
                        GoogleSignInDialogFragment.newInstance()
                                .setSignInListener {
                                    startActivityForResult(signInManager.signInIntent,
                                            GoogleSignInManager.rcSignIn)
                                }
                                .setMaybeLaterListener { signInManager.maybeLater = true }
                                .show(supportFragmentManager, "sign-in-fragment")
                    }
                }

            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menuItemBackup = menu.findItem(R.id.action_backup)
        menuItemGoogle = menu.findItem(R.id.action_google)
        menuItemLogin = menu.findItem(R.id.action_google_login)

        tryPersonalizeMenu()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Icepick.saveInstanceState(this, outState)
    }

    override fun onDelete(b: Book) {
        pagerAdapter.listener?.onBookDeleted(b)
        bookManager.removeBook(b.id)
    }

    override fun onShare(b: Book) {

        val sendIntent = DanteUtils.createSharingIntent(this, b)
        startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.send_to)))

        tracker.trackOnBookShared()
    }

    override fun onMoveToUpcoming(b: Book) {
        bookManager.updateBookState(b, Book.State.READ_LATER)
        pagerAdapter.listener?.onBookStateChanged(b, Book.State.READ_LATER)
    }

    override fun onMoveToCurrent(b: Book) {
        bookManager.updateBookState(b, Book.State.READING)
        pagerAdapter.listener?.onBookStateChanged(b, Book.State.READING)
    }

    override fun onMoveToDone(b: Book) {
        bookManager.updateBookState(b, Book.State.READ)
        pagerAdapter.listener?.onBookStateChanged(b, Book.State.READ)

        tracker.trackOnBookMovedToDone(b)
    }

    override fun onPageSelected(position: Int) {

        tabPosition = position
        appBar.setExpanded(true, true)
        toggleFab()
        animateHeader(position)
    }

    override fun onPageScrollStateChanged(state: Int) {}
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    // ---------------------------------------------------

    @OnClick(R.id.main_fab)
    fun onClickNewBook() {

        tracker.trackOnScanBook()

        startActivityForResult(BookRetrievalActivity.newIntent(this),
                AppParams.rcScanBook,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun initializeTabs() {

        // Setup the ViewPager
        pagerAdapter = BookPagerAdapter(applicationContext, supportFragmentManager)
        viewpager.adapter = pagerAdapter
        viewpager.removeOnPageChangeListener(this) // Remove first to avoid multiple listeners
        viewpager.addOnPageChangeListener(this)
        viewpager.offscreenPageLimit = 2
        tabLayout.setupWithViewPager(viewpager, false)

        // Select the tab
        val initialTab = tabLayout.getTabAt(tabPosition)
        initialTab?.select()

        // Set the icons of the tabs
        for (i in 0..tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.icon = ContextCompat.getDrawable(this, pagerAdapter.getPageIcon(i))
        }

        // Color the controls accordingly
        toolbar.setBackgroundColor(primaryOld)
        appBar.setBackgroundColor(primaryOld)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = primaryDarkOld
        }
    }

    private fun setupGoogleServices() {

        signInManager.setup(this)
        signInManager.isSignedIn(this).subscribe { isSignedIn ->

            if (isSignedIn) { // <- User signed in, TOP!
                connectToGoogleServices()
            } else if (!signInManager.maybeLater) { // <- user not signed in and did not opt-out
                GoogleSignInDialogFragment.newInstance()
                        .setSignInListener {
                            startActivityForResult(signInManager.signInIntent,
                                    GoogleSignInManager.rcSignIn)
                        }
                        .setMaybeLaterListener { signInManager.maybeLater = true }
                        .show(supportFragmentManager, "sign-in-fragment")
            }
        }
    }

    private fun connectToGoogleServices() {
        backupManager.connect(this)
        tryPersonalizeMenu()
    }

    private fun tryPersonalizeMenu() {

        val account = signInManager.getAccount(this)
        if (account != null) {
            if (account.photoUrl != null) {
                signInManager.loadAccountImage(this, account.photoUrl!!)
                        .subscribe({ bm ->
                            menuItemGoogle?.icon = AppUtils.createRoundedBitmap(this, bm)
                        }, { throwable: Throwable ->
                            throwable.printStackTrace()
                            showToast(R.string.error_google_photo)
                        })
            } else {
                menuItemGoogle?.icon = ContextCompat
                        .getDrawable(this, R.drawable.ic_user_template)
            }
            menuItemGoogle?.title = account.displayName
            menuItemLogin?.title = getString(R.string.Logout)
            menuItemBackup?.isEnabled = true
        } else {
            menuItemLogin?.title = getString(R.string.login)
            menuItemBackup?.isEnabled = false
            menuItemGoogle?.title = getString(R.string.menu_main_google_login)
            menuItemGoogle?.icon = ContextCompat.getDrawable(this, R.drawable.ic_google_white)
        }

    }

    private fun toggleFab() {
        fab.hide()
        Handler().postDelayed({ fab.show() }, 300)
    }

    private fun animateHeader(tab: Int) {

        var primary = 0
        var primaryDark = 0
        when (tab) {

            0 -> {
                primary = ContextCompat.getColor(this, R.color.tabcolor_upcoming)
                primaryDark = ContextCompat.getColor(this, R.color.tabcolor_upcoming_dark)
            }
            1 -> {
                primary = ContextCompat.getColor(this, R.color.tabcolor_current)
                primaryDark = ContextCompat.getColor(this, R.color.tabcolor_current_dark)
            }
            2 -> {
                primary = ContextCompat.getColor(this, R.color.tabcolor_done)
                primaryDark = ContextCompat.getColor(this, R.color.tabcolor_done_dark)
            }
        }

        val animatorAppBar = ObjectAnimator.ofObject(appBar, "backgroundColor",
                ArgbEvaluator(), primaryOld, primary)
                .setDuration(300)
        val animatorToolbar = ObjectAnimator.ofObject(toolbar, "backgroundColor",
                ArgbEvaluator(), primaryOld, primary)
                .setDuration(300)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(),
                primaryDarkOld, primaryDark)
                .setDuration(300)
        // Suppress lint, because we are only setting applyListener, when api is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAnimation.addUpdateListener { valueAnimator ->
                window.statusBarColor = valueAnimator.animatedValue as Int
            }
        }

        val set = AnimatorSet()
        set.playTogether(animatorAppBar, animatorToolbar, colorAnimation)
        set.start()

        primaryOld = primary
        primaryDarkOld = primaryDark
    }

    private fun showFabAwareSnackbar(text: String) {
        Snackbar.make(findViewById(R.id.main_content), text, Snackbar.LENGTH_SHORT).show()
    }

    private fun setupIcepick(savedInstanceState: Bundle?) {
        // Fields will be overwritten by icepick if they have already a value
        tabPosition = 1
        primaryOld = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
        primaryDarkOld = ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark)
        Icepick.restoreInstanceState(this, savedInstanceState)
    }

    private fun showGoogleWelcomeScreen(account: GoogleSignInAccount) {
        if (signInManager.showWelcomeScreen) {
            GoogleWelcomeScreenDialogFragment
                    .newInstance(account.givenName, account.photoUrl)
                    .setOnAcknowledgedListener {
                        signInManager.showWelcomeScreen = false
                    }
                    .show(supportFragmentManager, "google_welcome_dialog_fragment")
        }
    }

}