package at.shockbytes.dante.backup.provider.shockbytes.api

import at.shockbytes.dante.backup.model.BackupContent
import at.shockbytes.dante.backup.model.BackupMetadata
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Author:  Martin Macheiner
 * Date:    09.05.2019
 */
interface ShockbytesHerokuApi {

    @POST("identify")
    fun identify(
        @Header("Authorization") bearerToken: String
    ): Completable

    @GET("backups")
    fun listBackups(
        @Header("Authorization") bearerToken: String
    ): Single<List<BackupMetadata>>

    @DELETE("backups")
    fun removeAllBackups(
        @Header("Authorization") bearerToken: String
    ): Completable

    @GET("backup/{backupId}")
    fun getBooksBackupById(
        @Header("Authorization") bearerToken: String,
        @Path("backupId") backupId: String
    ): Single<BackupContent>

    @DELETE("backup/{backupId}")
    fun removeBackupById(
        @Header("Authorization") bearerToken: String,
        @Path("backupId") backupId: String
    ): Completable

    @PUT("backup")
    fun makeBackup(
        @Header("Authorization") bearerToken: String,
        @Body backupContent: BackupContent
    ): Single<BackupMetadata>

    companion object {
        const val SERVICE_ENDPOINT = "https://shockbytes-dante.herokuapp.com/"
    }
}