# Dante - Book Tracker

<a href='https://play.google.com/store/apps/details?id=at.shockbytes.dante&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' /></a>

Dante lets you manage all your books by simply scanning the ISBN barcode of the book. 
It will automatically grab all information from Googles book database. 
The app let's you arrange your books into 3 different categories, whether you 
have read the book, are currently reading the book or saved the book for later. So you 
can simply keep track of your progress of all your books and their current states.

## Versions

### Backlog
- [ ] Include Amazon books API?
- [ ] Change book cover and store data in Firebase Cloud Storage

### Version 3.3
- [ ] Use Firebase Data for book recommendations

### Version 3.2
- [ ] Firebase as Online Backend
- [ ] Redesign login flow with Firebase login

### Version 3.1
- [ ] Supporter's badge (in-app purchases)
- [ ] Add book manually (FAB menu -> Camera, By title, Manual)
- [ ] Tablet optimization

### Version 3.0 - FRESH FUN
- * Fix 'wrong dates' bug
- * Change dates after insertion
- * Refactor detail view
- * Sort book list
- * General Architecture redesign (abstract Realm to exchange it)
- * Fix backup mechanism
- * Fresh and new UI
- * Add current books to statistics (read pages to read, other pages to waiting)
- * Change Analytics backend (Keen -> Google/Firebase)

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
