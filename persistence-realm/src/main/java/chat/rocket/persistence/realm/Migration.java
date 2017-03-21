package chat.rocket.persistence.realm;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class Migration implements RealmMigration {
  @Override
  public void migrate(DynamicRealm dynamicRealm, long oldVersion, long newVersion) {

    RealmSchema schema = dynamicRealm.getSchema();

    if (oldVersion == 0) {
      RealmObjectSchema roomSchema = schema.get("RealmRoom");

      roomSchema.addField("f", boolean.class);

      oldVersion++;
    }
  }
}
