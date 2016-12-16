package chat.rocket.android.service.observer;

import android.content.Context;
import android.net.Uri;
import bolts.Task;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.api.FileUploadingHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.model.internal.FileUploading;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import io.realm.Realm;
import io.realm.RealmResults;
import java.io.InputStream;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

/**
 * execute file uploading and requesting sendMessage with attachment.
 */
public class FileUploadingToGridFsObserver extends AbstractModelObserver<FileUploading> {
  private FileUploadingHelper methodCall;

  public FileUploadingToGridFsObserver(Context context, String hostname,
      RealmHelper realmHelper, DDPClientWraper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
    methodCall = new FileUploadingHelper(realmHelper, ddpClient);

    realmHelper.executeTransaction(realm -> {
      // resume pending operations.
      RealmResults<FileUploading> pendingUploadRequests = realm.where(FileUploading.class)
          .equalTo("syncstate", SyncState.SYNCING)
          .equalTo("storageType", FileUploading.STORAGE_TYPE_GRID_FS)
          .findAll();
      for (FileUploading req : pendingUploadRequests) {
        req.setSyncstate(SyncState.NOT_SYNCED);
      }

      // clean up records.
      realm.where(FileUploading.class)
          .beginGroup()
          .equalTo("syncstate", SyncState.SYNCED)
          .or()
          .equalTo("syncstate", SyncState.FAILED)
          .endGroup()
          .equalTo("storageType", FileUploading.STORAGE_TYPE_GRID_FS)
          .findAll().deleteAllFromRealm();
      return null;
    }).continueWith(new LogcatIfError());
  }

  @Override public RealmResults<FileUploading> queryItems(Realm realm) {
    return realm.where(FileUploading.class)
        .equalTo("syncstate", SyncState.NOT_SYNCED)
        .equalTo("storageType", FileUploading.STORAGE_TYPE_GRID_FS)
        .findAll();
  }

  @Override public void onUpdateResults(List<FileUploading> results) {
    if (results.isEmpty()) {
      return;
    }

    List<FileUploading> uploadingList = realmHelper.executeTransactionForReadResults(realm ->
        realm.where(FileUploading.class).equalTo("syncstate", SyncState.SYNCING).findAll());
    if (uploadingList.size() >= 1) {
      // do not upload multiple files simultaneously
      return;
    }

    User currentUser = realmHelper.executeTransactionForRead(realm ->
        User.queryCurrentUser(realm).findFirst());
    Session session = realmHelper.executeTransactionForRead(realm ->
        Session.queryDefaultSession(realm).findFirst());
    if (currentUser == null || session == null) {
      return;
    }
    final String cookie = String.format("rc_uid=%s; rc_token=%s",
        currentUser.get_id(), session.getToken());

    FileUploading fileUploading = results.get(0);
    final String roomId = fileUploading.getRoomId();
    final String uplId = fileUploading.getUplId();
    final String filename = fileUploading.getFilename();
    final long filesize = fileUploading.getFilesize();
    final String mimeType = fileUploading.getMimeType();
    final Uri fileUri = Uri.parse(fileUploading.getUri());

    realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
            .put("uplId", uplId)
            .put("syncstate", SyncState.SYNCING)
        )
    ).onSuccessTask(_task -> methodCall.ufsCreate(filename, filesize, mimeType, roomId)
    ).onSuccessTask(task -> {
      final JSONObject info = task.getResult();
      final String fileId = info.getString("fileId");
      final String token = info.getString("token");
      final String url = info.getString("url");

      final int bufSize = 16384; //16KB
      final byte[] buffer = new byte[bufSize];
      int offset = 0;
      final MediaType contentType = MediaType.parse(mimeType);

      try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
        int read;
        while ((read = inputStream.read(buffer)) > 0) {
          offset += read;
          double progress = 1.0 * offset / filesize;

          Request request = new Request.Builder()
              .url(url + "&progress=" + progress)
              .header("Cookie", cookie)
              .post(RequestBody.create(contentType, buffer, 0, read))
              .build();

          Response response = OkHttpHelper.getClientForUploadFile().newCall(request).execute();
          if (response.isSuccessful()) {
            final JSONObject obj = new JSONObject()
                .put("uplId", uplId)
                .put("uploadedSize", offset);
            realmHelper.executeTransaction(realm ->
                realm.createOrUpdateObjectFromJson(FileUploading.class, obj));
          } else {
            return Task.forError(new Exception(response.message()));
          }
        }
      }

      return methodCall.ufsComplete(fileId, token);
    }).onSuccessTask(task -> methodCall.sendFileMessage(roomId, null, task.getResult())
    ).onSuccessTask(task -> realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
            .put("uplId", uplId)
            .put("syncstate", SyncState.SYNCED)
            .put("error", JSONObject.NULL)
        )
    )).continueWithTask(task -> {
      if (task.isFaulted()) {
        RCLog.w(task.getError());
        return realmHelper.executeTransaction(realm ->
            realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
                .put("uplId", uplId)
                .put("syncstate", SyncState.FAILED)
                .put("error", task.getError().getMessage())
            ));
      } else {
        return Task.forResult(null);
      }
    });
  }
}
