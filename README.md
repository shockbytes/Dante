# Dante - Book Tracker

Dante lets you manage all your books by simply scanning the ISBN barcode of the book. 
It will automatically grab all information from Googles book database. 
The app let's you arrange your books into 3 different categories, whether you 
have read the book, are currently reading the book or saved the book for later. So you 
can simply keep track of your progress of all your books and their current states.

<a href='https://play.google.com/store/apps/details?id=at.shockbytes.dante&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img height=100 alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>

| Home screen                             | Detail screen |  Book scanning screen                                             |
|:----------------------------------------|:--------------|:----------------------------------------------|
| ![Home screen](screenshots/en_main.png) | ![Details screen](screenshots/en_details.png)  | ![Book scanning screen](screenshots/en_scanning.png) |
## Setup
Checkout/Fork the repository and get the missing files which are mentioned below.
Make a pull request to the actual repository.

### Sensitive developer data
There are three files which must be explicitly added by each developer
after they checkout the project.

#### google-services.json
Get the `google-services.json` from the Firebase console.

#### /src/main/res/values/font_certs.xml
Those certs are important in order to load the custom fonts (Montserrat)
from the internet.

#### /src/main/res/values/oauth_client_id.xml
Download the client secret file from the Google Cloud Console. 
This file is necessary in order to retrieve the JWT request token of 
the user during the login process.

## Versions

### Backlog
Empty...

### Version 4.2
- [ ] Wishlist for books that are not purchased yet

### Version 4.1
- [ ] Use Firebase Data for book suggestions

### Version 4.0 - CAMPING WITH FIREBASE
- [ ] Add online sync capability
  - [ ] Migrate from local to remote storage

### Version 3.17
- [ ] Login with Firebase
- [ ] Add Onboarding + Login

### Version 3.16
- [ ] Send csv export via Mail
- [ ] Move actions into Book item (https://github.com/florent37/ExpansionPanel)
- [ ] Experimental remote storage Firestore implementation

### Version 3.15
- [ ] Statistics pages/books over time / month + Goal per month
    - [x] Fix issue where MarkerView draws out of ChartView bounds
    - [ ] Make books per month touchable
    - [ ] Show months where no books were read in books per month
    - [ ] Fix problems when setting books per month reading goal offset
    - [ ] Fix issues with pages per month reading goal update
    - [ ] Change toolbar behavior in Statistics screen
- [x] Reset page statistics per book
- [x] Hide page statistics in details page

### Version 3.14 - SUMMER CLEANUP
* Move sort into settings
* Improve Main UI
* Add pick random for reading button
* Pages statistics

### Version 3.13 - SMALL STEPS
* Update Crashlytics SDK
* Redesign overflow menu
* Timeline improvements (click on book, sort by start/end date)

### Version 3.12 - GO FLAT
* Go Flat: Flatten the whole layout
* Update to a stable CameraX version 1.0.0-beta03
* Desaturate label colors when in night mode

### Version 3.11 - IMPORT & EXPORT
* Allow Dante CSV import
* Dante CSV export
* Goodreads CSV import
* Deprecate Google Drive Backup
* Support remote book repository capability
* Rework Backup screen into Book management screen (Local and Online tab)

### Version 3.10 - SETTINGS AND TRACKING
* Dark Mode options DARK, LIGHT, SYSTEM_DEFAULT
* Change launcher icon
* Bring back tracking capabilities
* Smaller UI fixes
* Manual Add book cover bug

### Version 3.9 - LABELS
* Labels for books

### Version 3.8 - TIMELINE
* Timeline feature
* Various bugfixes

### Version 3.7 - SCARY SCANNING
* Redesign scan screen
* Improved dark mode

### Version 3.6 - GET EXCITED
* Move to Android App Bundles
* Improve Backups 
* Open source Dante

### Version 3.5 - ADD ANDROID FRAMEWORK AWESOMENESS
* App shortcuts
* App Widget
* Use this for FAB <https://github.com/sjwall/MaterialTapTargetPrompt>

### Version 3.4 - SUBTLE SEXY FEATURES
* Investigate shared element transition missing end anchor and check for layout bugs
* Refactor to AndroidX
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