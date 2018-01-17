# Dante - Book Tracker

<a href='https://play.google.com/store/apps/details?id=at.shockbytes.dante&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' /></a>

Dante lets you manage all your books by simply scanning the ISBN barcode of the book. 
It will automatically grab all information from Googles book database. 
The app let's you arrange your books into 3 different categories, whether you 
have read the book, are currently reading the book or saved the book for later. So you 
can simply keep track of your progress of all your books and their current states.

## Versions

### Backlog
- [ ] Add book manually
- [ ] Change book cover
- [ ] Include Amazon books API?

### Version 3.0
- [ ] Switch position of books in category with drag and drop?
- [ ] Book recommendation
- [ ] Ask user if it wants to send anonymous data to backend
- [ ] Upload scanned books to Firebase, or change backend as a whole to firebase?
- [ ] Include Proguard
- [ ] Add advertisement API (Add AdView in list)

### Version 2.8 - PAGES & BACKUP
- [ ] Show book page state as Overlay on cover in BookAdapter
- [ ] Improve statistics
- [ ] Auto backup

### Version 2.7 - SEARCHY SUBJECT
- [ ] Change date for book
- [ ] Search feature
- [ ] Tablet optimization
- [ ] Overflow menu with additional info (description, isbn, maybe more)

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
