package at.shockbytes.dante.util

import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.RealmSchema

/**
 * @author Martin Macheiner
 * Date: 13.02.2017.
 */

class DanteRealmMigration : RealmMigration {

    enum class Migrations {
        BASE, DATES, RATING_LANG, PAGES_NOTES
    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var versionCounter = oldVersion
        val schema = realm.schema

        if (versionCounter == Migrations.BASE.v()) {
            migrateDates(schema)
            versionCounter++
        }
        if (versionCounter == Migrations.DATES.v()) {
            migrateRatingAndLanguage(schema)
            versionCounter++
        }
        if (versionCounter == Migrations.RATING_LANG.v()) {
            migrateBookPageCountAndNotes(schema)
            // versionCounter++
        }
    }

    private fun migrateDates(schema: RealmSchema) {
        schema.get("Book")
                ?.addField("startDate", Long::class.java)
                ?.addField("endDate", Long::class.java)
                ?.addField("wishlistDate", Long::class.java)
    }

    private fun migrateRatingAndLanguage(schema: RealmSchema) {
        schema.get("Book")
                ?.addField("rating", Int::class.java)
                ?.addField("language", String::class.java)
    }

    private fun migrateBookPageCountAndNotes(schema: RealmSchema) {
        schema.get("Book")
                ?.addField("currentPage", Int::class.java)
                ?.addField("notes", String::class.java)
    }

    companion object {

        private fun Migrations.v(): Long = this.ordinal.toLong()

        val migrationVersion = Migrations.PAGES_NOTES.v()
    }

}
