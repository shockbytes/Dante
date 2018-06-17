# Dante - Book Tracker

<a href='https://play.google.com/store/apps/details?id=at.shockbytes.dante&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' /></a>

Dante lets you manage all your books by simply scanning the ISBN barcode of the book. 
It will automatically grab all information from Googles book database. 
The app let's you arrange your books into 3 different categories, whether you 
have read the book, are currently reading the book or saved the book for later. So you 
can simply keep track of your progress of all your books and their current states.

## Versions

### Backlog
- [ ] Add book manually (FAB menu)
- [ ] Include Amazon books API?
- [ ] Tablet optimization
- [ ] Use Firebase Data for book recommendations
- [ ] Firebase as Backend!
- [ ] Change book cover and store data in Firebase Cloud Storage
- [ ] Redesign login flow with Firebase login
- [ ] Supporting badge (in-app purchases)

### Version 3.0
- [ ] Change dates after insertion
- [ ] Fix 'backup overwrites wrong dates' bug
- [ ] Refactor detail view
- [x] Sort book list
- [x] General Architecture redesign (abstract Realm to exchange it)
- [x] Fix backup mechanism
- [x] Fresh and new UI
- [x] Add current books to statistics (read pages to read, other pages to waiting)
- [x] Change Analytics backend (Keen -> Google/Firebase)

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
