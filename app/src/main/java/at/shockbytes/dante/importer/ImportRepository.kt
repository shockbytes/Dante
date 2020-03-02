package at.shockbytes.dante.importer

import io.reactivex.Single
import java.io.File

interface ImportRepository {

    fun import(importer: Importer, file: File): Single<ImportStats>
}