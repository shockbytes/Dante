package at.shockbytes.dante.importer

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface ImportRepository {

    fun parse(importer: Importer, content: String): Single<ImportStats>

    fun import(): Completable
}