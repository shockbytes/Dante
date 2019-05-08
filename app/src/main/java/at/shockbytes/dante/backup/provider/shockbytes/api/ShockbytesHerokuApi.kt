package at.shockbytes.dante.backup.provider.shockbytes.api

import at.shockbytes.dante.backup.model.BackupEntry
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Author:  Martin Macheiner
 * Date:    09.05.2019
 */
interface ShockbytesHerokuApi {

    @GET("backups")
    fun listBackups(
        @Header("Authorization") bearerToken: String
    ): Single<List<BackupEntry>>

    @GET("backup/restore/{backupId}")
    fun getBackupById(
        @Header("Authorization") bearerToken: String,
        @Path("backupId") backupId: String
    ): Single<List<BackupEntry>>

    companion object {
        const val SERVICE_ENDPOINT = "https://192.168.0.220:443/"
    }
}