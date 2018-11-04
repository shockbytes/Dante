# Dante - Book Tracker

Dante lets you manage all your books by simply scanning the ISBN barcode of the book. 
It will automatically grab all information from Googles book database. 
The app let's you arrange your books into 3 different categories, whether you 
have read the book, are currently reading the book or saved the book for later. So you 
can simply keep track of your progress of all your books and their current states.

## Versions

### Version 4.0 - ALL IN THE CLOUD
- [ ] Firebase as Online Backend

### Version 3.3 - CAMPING WITH FIREBASE
- [ ] Redesign login flow with Firebase login
- [ ] Use Firebase Data for book suggestions

### Version 3.2 -
- [ ] Include book description in Download
- [ ] Replace preferences UI
- [ ] Reading history in statistics
- [ ] Dark mode


### Version 3.1 - SUPPORT STATISTICS!
- [ ] Test In-app purchases
- [ ] Statistics fixes and Redesign
- [x] Abstract Glide usage with interface and object class
- [x] Sort by pages
- [x] Flatten UI
- [x] Use Timber with Crashlytics Tree and increase logging
- [x] Add book manually
- [x] Supporter's badge
- [x] Integrate feature flagging and config platform

#### TODO Before 3.1 release
- [ ] Fix ripple colors of buttons
- [ ] Fix nasty view bug when entering and exiting other activities
- [ ] Fix thumbnail overlay not rounded corners bug
- [ ] Translate strings!
- [ ] Translate to italian

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
