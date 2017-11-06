package chat.rocket.android.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import bolts.Task;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.persistence.realm.RealmHelper;

/**
 * MethodCall for uploading file.
 */
public class FileUploadingHelper extends MethodCallHelper {
  public FileUploadingHelper(Context context, String hostname) {
    super(context, hostname);
  }

  public FileUploadingHelper(RealmHelper realmHelper) {
    super(realmHelper);
  }

  public Task<JSONObject> uploadS3Request(String filename, long filesize, String mimeType,
                                          String roomId) {
    return uploadRequest("rocketchat-uploads", filename, filesize, mimeType, roomId);
  }

  public Task<JSONObject> uploadGoogleRequest(String filename, long filesize, String mimeType,
                                              String roomId) {
    return uploadRequest("rocketchat-uploads-gs", filename, filesize, mimeType, roomId);
  }

  public Task<Void> sendFileMessage(String roomId, String storageType, JSONObject fileObj) {
    return call("sendFileMessage", TIMEOUT_MS, () -> new JSONArray()
        .put(roomId)
        .put(TextUtils.isEmpty(storageType) ? JSONObject.NULL : storageType)
        .put(fileObj))
        .onSuccessTask(task -> Task.forResult(null));
  }

  public Task<JSONObject> ufsCreate(String filename, long filesize, String mimeType, String store,
                                    String roomId) {
    return call("ufsCreate", TIMEOUT_MS, () -> new JSONArray().put(new JSONObject()
        .put("name", filename)
        .put("size", filesize)
        .put("type", mimeType)
        .put("store", store)
        .put("rid", roomId)
    )).onSuccessTask(CONVERT_TO_JSON_OBJECT);
  }

  public Task<JSONObject> ufsComplete(String fileId, String token, String store) {
    return call("ufsComplete", TIMEOUT_MS, () -> new JSONArray()
        .put(fileId)
        .put(store)
        .put(token)
    ).onSuccessTask(CONVERT_TO_JSON_OBJECT);
  }

  private Task<JSONObject> uploadRequest(String uploadType, String filename,
                                         long filesize, String mimeType,
                                         String roomId) {
    return call("slingshot/uploadRequest", TIMEOUT_MS, () -> new JSONArray()
        .put(uploadType)
        .put(new JSONObject()
            .put("name", filename)
            .put("size", filesize)
            .put("type", mimeType))
        .put(new JSONObject().put("rid", roomId)))
        .onSuccessTask(CONVERT_TO_JSON_OBJECT);
  }
}
