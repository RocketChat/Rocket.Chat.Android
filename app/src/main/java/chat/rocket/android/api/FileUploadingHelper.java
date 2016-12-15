package chat.rocket.android.api;

import android.content.Context;
import bolts.Task;
import chat.rocket.android.realm_helper.RealmHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * MethodCall for uploading file.
 */
public class FileUploadingHelper extends MethodCallHelper {
  public FileUploadingHelper(Context context, String serverConfigId) {
    super(context, serverConfigId);
  }

  public FileUploadingHelper(RealmHelper realmHelper, DDPClientWraper ddpClient) {
    super(realmHelper, ddpClient);
  }

  public Task<JSONObject> uploadRequest(String filename, long filesize, String mimeType,
      String roomId) {
    return call("slingshot/uploadRequest", TIMEOUT_MS, () -> new JSONArray()
        .put("rocketchat-uploads")
        .put(new JSONObject()
            .put("name", filename)
            .put("size", filesize)
            .put("type", mimeType))
        .put(new JSONObject().put("rid", roomId)))
        .onSuccessTask(CONVERT_TO_JSON_OBJECT);
  }

  public Task<JSONObject> sendFileMessage(String roomId, String repository, JSONObject fileObj) {
    return call("sendFileMessage", TIMEOUT_MS, () -> new JSONArray()
        .put(roomId)
        .put(repository)
        .put(fileObj))
        .onSuccessTask(CONVERT_TO_JSON_OBJECT);
  }
}
