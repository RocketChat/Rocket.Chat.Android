package chat.rocket.persistence.realm.models.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import chat.rocket.core.models.SpotlightRoom;

public class RealmSpotlightRoom extends RealmObject {

  public interface Columns {
    String ID = "_id";
    String NAME = "name";
    String TYPE = "t";
  }

  @PrimaryKey private String _id;
  private String name;
  private String t;

  public String getId() {
    return _id;
  }

  public void setId(String _id) {
    this._id = _id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return t;
  }

  public void setType(String t) {
    this.t = t;
  }

  public SpotlightRoom asSpotlightRoom() {
    return SpotlightRoom.builder()
        .setId(_id)
        .setName(name)
        .setType(t)
        .build();
  }
}