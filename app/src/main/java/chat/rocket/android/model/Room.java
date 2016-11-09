package chat.rocket.android.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Chat Room.
 */
@SuppressWarnings("PMD.ShortVariable")
public class Room extends RealmObject {
  @PrimaryKey private String _id;
  private String serverConfigId;
  private String name;
  private String t; //type { c: channel, d: direct message, p: private }
  private User u; //User who created this room.
  private String topic;
}
