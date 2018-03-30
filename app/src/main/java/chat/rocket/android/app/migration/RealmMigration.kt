package chat.rocket.android.app.migration

import chat.rocket.android.BuildConfig
import chat.rocket.android.app.migration.model.RealmUser
import io.realm.DynamicRealm
import io.realm.RealmMigration

class RealmMigration : RealmMigration {
    override fun migrate(dynamicRealm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var oldVersion = oldVersion

        val schema = dynamicRealm.schema

        if (oldVersion == 0L) {
            // NOOP
            oldVersion++
        }

        if (oldVersion == 1L) {
            oldVersion++
        }

        if (oldVersion == 2L) {
            oldVersion++
        }

        if (oldVersion == 3L) {
            oldVersion++
        }

        if (oldVersion == 4L) {

            oldVersion++
        }

        if (oldVersion == 5L) {
            val userSchema = schema.get("RealmUser")
            try {
                userSchema?.addField(RealmUser.NAME, String::class.java)
            } catch (e: IllegalArgumentException) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
                // ignore; it makes here if the schema for this model was already update before without migration
            }

        }
    }

    // hack around to avoid "new different configuration cannot access the same file" error
    override fun hashCode(): Int {
        return 37
    }

    override fun equals(o: Any?): Boolean {
        return o is chat.rocket.android.app.migration.RealmMigration
    }
    // end hack
}