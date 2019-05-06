# Dante - Book Tracker

Dante lets you manage all your books by simply scanning the ISBN barcode of the book. 
It will automatically grab all information from Googles book database. 
The app let's you arrange your books into 3 different categories, whether you 
have read the book, are currently reading the book or saved the book for later. So you 
can simply keep track of your progress of all your books and their current states.

## Versions


### Version 4.0 - SUGGESTIONS
- [ ] Use Firebase Data for book suggestions
- [ ] Italian language support

### Version 3.9 - ALL IN THE CLOUD
- [ ] Firebase as Online Backend
- [ ] Remove backup functionality / Replace it with online sync

### Version 3.8 - CLEAN THE CAMPFIRE BEFORE THE BBQ
- [ ] Put overflow menu into ActionBar
- [ ] Labels for books

### Version 3.7 - START A CAMPFIRE
- [ ] Move to Android App Bundles
- [ ] Redesign login flow with Firebase login
- [ ] Add onboarding

### Version 3.5
- [x] App shortcuts
- [x] App Widget
    - [x] Update list on app close 
    - [x] Do not show scrollbar on rounded corner
    - [x] Open book on click
- [ ] Use this for FAB <https://github.com/sjwall/MaterialTapTargetPrompt>

### Version 3.4 - SUBTLE SEXY FEATURES
* Investigate shared element transition missing end anchor and check for layout bugs
* Refactor to Android X
* Add UI for Feature flags
* Add +/- buttons for page overlay
* Android 5 as minSdk

### Version 3.3 - DETAILS & DEBTS
* Rework notes screen (give it more space)
* Allow users to add a summary in the manual add
* New details page design
* Disable summary in settings
* Change icon color of settings depending on if night mode or not
* Fix BaseAdapter bug
* Enable language selection for manual add
* Replace ImagePicker library with <https://github.com/qingmei2/RxImagePicker>
* Streamline Realm and move the query off the main thread (Provider pattern)
* Remove In-app purchases logic (or encapsulate it properly)
* Adaption of the main card

### Version 3.2 - SMALL, STEADY IMPROVEMENTS
* Include book description in Download
* Improve dark mode
* Improve search view (refactor with ViewModel)
* Improve preferences UI
* Fix layout bugs of MainActivity
* Abstract usage of ImagePicker
* Fix images for overflow menu

### Version 3.1 - DARK STATISTICS FIXES
* Dark mode
* Statistics fixes and Redesign
* Sort by pages
* Flatten UI
* Add books manually
* Abstract Glide usage with interface and object class
* Use Timber with Crashlytics Tree and increase logging
* Supporter's badge
* Integrate feature flagging and config platform

### Version 3.0 - FRESH FUN
* Fix 'wrong dates' bug
* Change dates after insertion
* Refactor detail view
* Sort book list
* General Architecture redesign (abstract Realm to exchange it)
* Fix backup mechanism
* Fresh and new UI
* Add current books to statistics (read pages to read, other pages to waiting)
* Change Analytics backend (Keen -> Google/Firebase)

### Version 2.8 - PAGES, POSITION, PROGUARD
* Include Proguard
* Show book page state as Overlay on cover in BookAdapter
* Switch position of books in category with drag and drop

### Version 2.7 - SEARCHY STATS
* Change publish date for book
* Improve statistics screen
* Search feature

### Version 2.6 - DETAILED DESIGN
* Rate books 
* 100% Kotlin Port if possible
* Enter book page count manually 
* Adding notes to books

### Version 2.5 - REFACTOR RAMPAGE

* Introduce utility classes (BaseFragment, BaseActivity, BackNavigableActivity)
* Introduce KotterKnife
* Update to newest ButterKnife version
* Improve backup api
* Introduce GoogleSignIn
* Add Crashlytics
* Code cleanup and Kotlin Port
* Introduction / Showcase View
* DownloadBook / QueryCapture Activity merging 
* ViewPagerAdapter
* Adaptive Icons
