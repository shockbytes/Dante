package at.shockbytes.dante.util;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * @author Martin Macheiner
 *         Date: 13.02.2017.
 */

public class DanteRealmMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            migrateDates(schema);
            oldVersion++;
        }
        if (oldVersion == 1) {
            migrateRatingAndLanguage(schema);
            //oldVersion++;
        }
    }

    private void migrateDates(RealmSchema schema) {
        schema.get("Book")
                .addField("startDate", long.class)
                .addField("endDate", long.class)
                .addField("wishlistDate", long.class);
    }

    private void migrateRatingAndLanguage(RealmSchema schema) {
        schema.get("Book")
                .addField("rating", int.class)
                .addField("language", String.class);
    }
}
