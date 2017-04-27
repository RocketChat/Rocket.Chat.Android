package chat.rocket.persistence.realm.models.ddp;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.core.models.Role;
import chat.rocket.core.models.RoomRole;

public class RealmRoomRole extends RealmObject {

  public interface Columns {
    String ID = "_id";
    String ROOM_ID = "rid";
    String USER = "u";
    String ROLES = "roles";
  }

  @PrimaryKey private String _id;
  private String rid;
  private RealmUser u;
  private RealmList<RealmRole> roles;

  public static JSONObject customizeJson(JSONObject roomRoles) throws JSONException {
    JSONArray roleStrings = roomRoles.getJSONArray(Columns.ROLES);
    JSONArray roles = new JSONArray();
    for (int i = 0, size = roleStrings.length(); i < size; i++) {
      roles.put(RealmRole.customizeJson(roleStrings.getString(i)));
    }

    roomRoles.remove(Columns.ROLES);
    roomRoles.put(Columns.ROLES, roles);

    return roomRoles;
  }

  public String getId() {
    return _id;
  }

  public void setId(String id) {
    this._id = id;
  }

  public String getRoomId() {
    return rid;
  }

  public void setRoomId(String roomId) {
    this.rid = roomId;
  }

  public RealmUser getUser() {
    return u;
  }

  public void setUser(RealmUser user) {
    this.u = user;
  }

  public RealmList<RealmRole> getRoles() {
    return roles;
  }

  public void setRoles(RealmList<RealmRole> roles) {
    this.roles = roles;
  }

  public RoomRole asRoomRole() {
    int size = this.roles.size();
    List<Role> roles = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      roles.add(this.roles.get(i).asRole());
    }

    return RoomRole.builder()
        .setId(_id)
        .setRoomId(rid)
        .setUser(u.asUser())
        .setRoles(roles)
        .build();
  }
}
