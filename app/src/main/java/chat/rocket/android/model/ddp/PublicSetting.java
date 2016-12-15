package chat.rocket.android.model.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * public setting model.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class PublicSetting extends RealmObject {
  @PrimaryKey private String _id;
  private String group;
  private String type;
  private String value; //any type is available...!
  private long _updatedAt;
  private String meta; //JSON

  public static JSONObject customizeJson(JSONObject settingJson) throws JSONException {
    if (!settingJson.isNull("_updatedAt")) {
      long updatedAt = settingJson.getJSONObject("_updatedAt").getLong("$date");
      settingJson.remove("_updatedAt");
      settingJson.put("_updatedAt", updatedAt);
    }

    return settingJson;
  }
}
