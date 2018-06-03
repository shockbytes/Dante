package at.shockbytes.dante.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.MenuItem
import at.shockbytes.dante.R
import at.shockbytes.dante.adapter.BookAdapter
import at.shockbytes.dante.adapter.BookPagerAdapter
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.signin.DanteUser
import at.shockbytes.dante.signin.GoogleSignInManager
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.fragment.MenuFragment
import at.shockbytes.dante.ui.fragment.dialog.GoogleSignInDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.GoogleWelcomeScreenDialogFragment
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.loadBitmap
import at.shockbytes.dante.util.toggle
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.util.AppUtils
import com.crashlytics.android.Crashlytics
import icepick.Icepick
import icepick.State
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity(), BookAdapter.OnBookPopupItemSelectedListener,
        ViewPager.OnPageChangeListener {

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
    protected var tabId: Int = 0

    private lateinit var pagerAdapter: BookPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupIcepick(savedInstanceState)
        setupGoogleServices()
        initializeNavigation()
        setupUI()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onDestroy() {
        backupManager.close()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            DanteUtils.rcAddBook -> {
                if (resultCode == RESULT_OK) {
                    val bookId = data?.getLongExtra(DanteUtils.extraBookId, -1) ?: -1
                    if (bookId > -1) {
                        pagerAdapter.listener?.onBookAdded(bookManager.getBook(bookId))
                    }
                }
            }
            GoogleSignInManager.rcSignIn -> {
                if (data != null) {
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
            }
            BackupActivity.rcBackupRestored -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Backup is restored, data no longer valid --> refresh
                    initializeNavigation()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // TODO Put this into the view Model!
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

        return super.onOptionsItemSelected(item)
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

        tabId = mainBottomNavigation.menu.getItem(position).itemId
        mainBottomNavigation.selectedItemId = tabId

        appBar.setExpanded(true, true)
        fab.toggle()
    }

    override fun onPageScrollStateChanged(state: Int) {}
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

// ---------------------------------------------------

    private fun setupUI() {

        fab.setOnClickListener {
            tracker.trackOnScanBook()

            startActivityForResult(BookRetrievalActivity.newIntent(this),
                    DanteUtils.rcAddBook,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
        }

        imgButtonMainToolbarSearch.setOnClickListener {
            startActivityForResult(SearchActivity.newIntent(this),
                    DanteUtils.rcAddBook,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
        }

        imgButtonMainToolbarMore.setOnClickListener {
            MenuFragment.newInstance(signInManager.getAccount(this))
                    .show(supportFragmentManager, "menu-fragment")
        }
    }

    private fun initializeNavigation() {

        // Setup the ViewPager
        pagerAdapter = BookPagerAdapter(applicationContext, false, supportFragmentManager)
        viewPager.adapter = pagerAdapter
        viewPager.removeOnPageChangeListener(this) // Remove first to avoid multiple listeners
        viewPager.addOnPageChangeListener(this)
        viewPager.offscreenPageLimit = 2

        mainBottomNavigation.setOnNavigationItemSelectedListener { item ->

            colorNavigationItems(item)
            DanteUtils.indexForNavigationItemId(item.itemId)?.let { viewPager.currentItem = it }

            true
        }

        mainBottomNavigation.selectedItemId = tabId
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

        account?.photoUrl?.let { photoUrl ->
            photoUrl.loadBitmap(this).subscribe({ bm ->
                imgButtonMainToolbarMore.setImageDrawable(AppUtils.createRoundedBitmap(this, bm))
            }, { throwable: Throwable ->
                throwable.printStackTrace()
            })
        }
    }

    private fun colorNavigationItems(item: MenuItem) {

        val stateListRes: Int = when (item.itemId) {
            R.id.menu_navigation_upcoming -> R.drawable.navigation_item_upcoming
            R.id.menu_navigation_current -> R.drawable.navigation_item_current
            R.id.menu_navigation_done -> R.drawable.navigation_item_done
            // TODO Enable later R.id.menu_navigation_suggestions -> R.drawable.navigation_item_suggestions
            else -> 0
        }

        val stateList = ContextCompat.getColorStateList(this, stateListRes)
        mainBottomNavigation.itemIconTintList = stateList
        mainBottomNavigation.itemTextColor = stateList
    }

    private fun showFabAwareSnackbar(text: String) {
        Snackbar.make(findViewById(R.id.main_content), text, Snackbar.LENGTH_SHORT).show()
    }

    private fun setupIcepick(savedInstanceState: Bundle?) {
        // Fields will be overwritten by icepick if they have already a value
        tabId = R.id.menu_navigation_current
        Icepick.restoreInstanceState(this, savedInstanceState)
    }

    private fun showGoogleWelcomeScreen(account: DanteUser) {
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