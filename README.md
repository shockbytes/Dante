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

### Version 3.5 - ALL IN THE CLOUD
- [ ] Firebase as Online Backend
- [ ] Remove backup functionality / Replace it with online sync

### Version 3.4 - START A CAMPFIRE
- [ ] Redesign login flow with Firebase login
- [ ] Some sort of onboarding

### Version 3.3 - DETAILS & DEBTS
- [ ] New details page design
- [ ] Disable summary in settings
- [ ] Rework notes screen (give it more space)
- [ ] Put overflow menu into ActionBar
- [ ] Allow users to add a summary in the manual add
- [ ] Change icon color of settings depending if night mode or not
- [x] Fix BaseAdapter bug
- [x] Enable language selectiion for manual add
- [x] Replace ImagePicker library with https://github.com/qingmei2/RxImagePicker
- [x] Streamline Realm and move querying off the main thread (Provider pattern)
- [x] Remove In-app purchases logic (or encapsulate it properly)
- [x] Adaption of main card

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
