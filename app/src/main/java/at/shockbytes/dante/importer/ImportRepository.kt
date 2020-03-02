package at.shockbytes.dante.importer

import io.reactivex.Completable
import io.reactivex.Single

interface ImportRepository {

    fun parse(importer: Importer, content: String): Single<ImportStats>

    fun import(): Completable
}