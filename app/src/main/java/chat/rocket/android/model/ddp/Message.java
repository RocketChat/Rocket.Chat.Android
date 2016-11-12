package chat.rocket.android.model.ddp;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Message.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class Message extends RealmObject {
  @PrimaryKey private String _id;
  private String t; //type:
  private String rid; //roomId.
  private long ts;
  private String msg;
  private User u;
  private boolean groupable;
  private RealmList<MessageAttachment> attachments;
  private RealmList<MessageUrl> urls;


  public static JSONObject customizeJson(JSONObject messageJson) throws JSONException {
    long ts = messageJson.getJSONObject("ts").getLong("$date");
    messageJson.remove("ts");
    messageJson.put("ts", ts);

    return messageJson;
  }
}
