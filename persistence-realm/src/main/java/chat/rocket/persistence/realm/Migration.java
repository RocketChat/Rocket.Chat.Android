package chat.rocket.persistence.realm;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

import chat.rocket.persistence.realm.models.ddp.RealmMessage;
import chat.rocket.persistence.realm.models.ddp.RealmPermission;
import chat.rocket.persistence.realm.models.ddp.RealmRole;
import chat.rocket.persistence.realm.models.ddp.RealmRoomRole;
import chat.rocket.persistence.realm.models.ddp.RealmSpotlightRoom;
import chat.rocket.persistence.realm.models.ddp.RealmSpotlightUser;

public class Migration implements RealmMigration {
  @Override
  public void migrate(DynamicRealm dynamicRealm, long oldVersion, long newVersion) {

    RealmSchema schema = dynamicRealm.getSchema();

    if (oldVersion == 0) {
      // NOOP
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

      oldVersion++;
    }

    if (oldVersion == 3) {
      schema.create("RealmRole")
          .addField(RealmRole.Columns.ID, String.class, FieldAttribute.PRIMARY_KEY)
          .addField(RealmRole.Columns.NAME, String.class);

      schema.create("RealmPermission")
          .addField(RealmPermission.Columns.ID, String.class, FieldAttribute.PRIMARY_KEY)
          .addField(RealmPermission.Columns.NAME, String.class)
          .addRealmListField(RealmPermission.Columns.ROLES, schema.get("RealmRole"));

      schema.create("RealmRoomRole")
          .addField(RealmRoomRole.Columns.ID, String.class, FieldAttribute.PRIMARY_KEY)
          .addField(RealmRoomRole.Columns.ROOM_ID, String.class)
          .addRealmObjectField(RealmRoomRole.Columns.USER, schema.get("RealmUser"))
          .addRealmListField(RealmRoomRole.Columns.ROLES, schema.get("RealmRole"));

      oldVersion++;
    }

    if (oldVersion == 4) {
      RealmObjectSchema messageSchema = schema.get("RealmMessage");
      messageSchema.addField(RealmMessage.EDITED_AT, long.class);
    }
  }

  // hack around to avoid "new different configuration cannot access the same file" error
  @Override
  public int hashCode() {
    return 37;
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof Migration);
  }
  // end hack
}
