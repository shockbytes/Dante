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
- [ ] Use Firebase Data for book suggestions
- [ ] Add online sync capability
- [ ] Add onboarding
- [ ] Introduce Shockbytes Backups
- [ ] Redesign overflow menu

### Version 3.9 - LABELS
- [ ] Labels for books
- [ ] Dark Mode options DARK, LIGHT, SYSTEM_DEFAULT
- [ ] Scanning crashes

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

### Version 3.5
* App shortcuts
* App Widget
* Use this for FAB <https://github.com/sjwall/MaterialTapTargetPrompt>

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