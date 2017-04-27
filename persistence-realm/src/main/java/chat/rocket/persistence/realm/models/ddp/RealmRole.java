package chat.rocket.persistence.realm.models.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.core.models.Role;

public class RealmRole extends RealmObject {

  public interface Columns {
    String ID = "id";
    String NAME = "name";
  }

  @PrimaryKey private String id;
  private String name;

  public static JSONObject customizeJson(String roleString) throws JSONException {
    JSONObject roleObject = new JSONObject();

    roleObject.put(Columns.ID, roleString);
    roleObject.put(Columns.NAME, roleString);

    return roleObject;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Role asRole() {
    return Role.builder()
        .setId(id)
        .setName(name)
        .build();
  }
}
