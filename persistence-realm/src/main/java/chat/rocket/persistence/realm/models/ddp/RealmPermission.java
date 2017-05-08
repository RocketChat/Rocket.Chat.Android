package chat.rocket.persistence.realm.models.ddp;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.core.models.Permission;
import chat.rocket.core.models.Role;

public class RealmPermission extends RealmObject {

  public interface Columns {
    String ID = "_id";
    String NAME = "name";
    String ROLES = "roles";
  }

  @PrimaryKey private String _id;
  private String name;
  private RealmList<RealmRole> roles;

  public static JSONObject customizeJson(JSONObject permissionsJson) throws JSONException {
    permissionsJson.put(Columns.NAME, permissionsJson.getString(Columns.ID));

    JSONArray roleStrings = permissionsJson.getJSONArray(Columns.ROLES);
    JSONArray roles = new JSONArray();
    for (int i = 0, size = roleStrings.length(); i < size; i++) {
      roles.put(RealmRole.customizeJson(roleStrings.getString(i)));
    }

    permissionsJson.remove(Columns.ROLES);
    permissionsJson.put(Columns.ROLES, roles);

    return permissionsJson;
  }

  public String getId() {
    return _id;
  }

  public void setId(String id) {
    this._id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public RealmList<RealmRole> getRoles() {
    return roles;
  }

  public void setRoles(RealmList<RealmRole> roles) {
    this.roles = roles;
  }

  public Permission asPermission() {
    int size = this.roles.size();
    List<Role> roles = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      roles.add(this.roles.get(i).asRole());
    }

    return Permission.builder()
        .setId(_id)
        .setName(name)
        .setRoles(roles)
        .build();
  }
}
