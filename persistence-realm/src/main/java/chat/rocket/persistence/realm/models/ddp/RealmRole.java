package chat.rocket.persistence.realm.models.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONException;
import org.json.JSONObject;

public class RealmRole extends RealmObject {

  public static JSONObject customizeJson(String roleString) throws JSONException {
    JSONObject roleObject = new JSONObject();

    roleObject.put(Columns.ID, roleString);
    roleObject.put(Columns.NAME, roleString);

    return roleObject;
  }

  public interface Columns {
    String ID = "id";
    String NAME = "name";
  }

  @PrimaryKey private String id;
  private String name;

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
}
