package at.shockbytes.dante.util;

import android.support.annotation.NonNull;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * @author Martin Macheiner
 *         Date: 13.02.2017.
 */

public class DanteRealmMigration implements RealmMigration {

    @Override
    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            migrateDates(schema);
            oldVersion++;
        }
        if (oldVersion == 1) {
            migrateRatingAndLanguage(schema);
            oldVersion++;
        }
        if (oldVersion == 2) {
            migrateBookPageCountAndNotes(schema);
            oldVersion++;
        }
    }

    private void migrateDates(RealmSchema schema) {
        schema.get("Book")
                .addField("startDate", long.class)
                .addField("endDate", long.class)
                .addField("wishlistDate", long.class);
    }

    private void migrateRatingAndLanguage(RealmSchema schema) {

        // FIXME look into this
        try {
            schema.get("Book")
                    .addField("rating", int.class)
                    .addField("language", String.class);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void migrateBookPageCountAndNotes(RealmSchema schema) {
        schema.get("Book")
                .addField("currentPage", int.class)
                .addField("notes", String.class);
    }

}
