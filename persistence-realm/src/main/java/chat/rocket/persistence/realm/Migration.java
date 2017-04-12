package chat.rocket.persistence.realm;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

import chat.rocket.persistence.realm.models.ddp.RealmSpotlightRoom;
import chat.rocket.persistence.realm.models.ddp.RealmSpotlightUser;

public class Migration implements RealmMigration {
  @Override
  public void migrate(DynamicRealm dynamicRealm, long oldVersion, long newVersion) {

    RealmSchema schema = dynamicRealm.getSchema();

    if (oldVersion == 0) {
      RealmObjectSchema roomSchema = schema.get("RealmRoom");

      roomSchema.addField("f", boolean.class);

      oldVersion++;
    }

    if (oldVersion == 1) {
      schema.create("RealmSpotlightUser")
          .addField(RealmSpotlightUser.Columns.ID, String.class, FieldAttribute.PRIMARY_KEY)
          .addField(RealmSpotlightUser.Columns.USERNAME, String.class)
          .addField(RealmSpotlightUser.Columns.STATUS, String.class);

      schema.create("RealmSpotlightRoom")
          .addField(RealmSpotlightRoom.Columns.ID, String.class, FieldAttribute.PRIMARY_KEY)
          .addField(RealmSpotlightRoom.Columns.NAME, String.class)
          .addField(RealmSpotlightRoom.Columns.TYPE, String.class);

      oldVersion++;
    }

    if (oldVersion == 2) {
      RealmObjectSchema roomSchema = schema.get("RealmSpotlightUser");

      roomSchema.addField(RealmSpotlightUser.Columns.NAME, String.class);
    }
  }
}
