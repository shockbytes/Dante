package at.shockbytes.dante.core.data.local

import at.shockbytes.dante.core.book.BookIds
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.book.realm.RealmBookLabel
import at.shockbytes.dante.core.book.realm.RealmInstanceProvider
import at.shockbytes.dante.core.data.BookLabelDao
import at.shockbytes.dante.util.completableOf
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.realm.Sort

class RealmBookLabelDao(
    private val realm: RealmInstanceProvider
): BookLabelDao {

    private val labelMapper = RealmBookLabelMapper()
    private val labelClass = RealmBookLabel::class.java

    override val bookLabelObservable: Observable<List<BookLabel>>
        get() = realm.read<RealmBookLabel>()
            .equalTo("bookId", BookIds.default())
            .sort("title", Sort.DESCENDING)
            .distinct("title")
            .findAllAsync()
            .asFlowable()
            .map { labelMapper.mapTo(it) }
            .toObservable()
            // Required as long as Realm has no built-in RxJava3 support
            .let(RxJavaBridge::toV3Observable)

    override fun createBookLabel(bookLabel: BookLabel): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.copyToRealm(labelMapper.mapFrom(bookLabel))
            }
        }
    }

    override fun deleteBookLabel(bookLabel: BookLabel): Completable {
        return Completable.create { emitter ->
            realm.executeTransaction { realm ->
                val labels = realm.where(labelClass)
                    .equalTo("title", bookLabel.title)
                    .and()
                    .equalTo("bookId", bookLabel.bookId)
                    .findAll()

                if (labels != null && labels.isNotEmpty()) {
                    labels.forEach { rbl ->
                        rbl.deleteFromRealm()
                    }
                    emitter.onComplete()
                } else {
                    emitter.tryOnError(RealmBookLabelDeletionException(bookLabel.title))
                }
            }
        }
    }
}