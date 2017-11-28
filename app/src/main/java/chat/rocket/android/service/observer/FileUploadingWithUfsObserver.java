package chat.rocket.android.service.observer;

import android.content.Context;
import android.net.Uri;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

import bolts.Task;
import chat.rocket.android.api.FileUploadingHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.models.internal.FileUploading;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * execute file uploading and requesting sendMessage with attachment.
 */
public class FileUploadingWithUfsObserver extends AbstractModelObserver<FileUploading> {
  private FileUploadingHelper methodCall;

  public FileUploadingWithUfsObserver(Context context, String hostname,
                                      RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
    methodCall = new FileUploadingHelper(realmHelper);

    realmHelper.executeTransaction(realm -> {
      // resume pending operations.
      RealmResults<FileUploading> pendingUploadRequests = realm.where(FileUploading.class)
          .equalTo(FileUploading.SYNC_STATE, SyncState.SYNCING)
          .beginGroup()
          .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_GRID_FS)
          .or()
          .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_FILE_SYSTEM)
          .endGroup()
          .findAll();
      for (FileUploading req : pendingUploadRequests) {
        req.setSyncState(SyncState.NOT_SYNCED);
      }

      // clean up records.
      realm.where(FileUploading.class)
          .beginGroup()
          .equalTo(FileUploading.SYNC_STATE, SyncState.SYNCED)
          .or()
          .equalTo(FileUploading.SYNC_STATE, SyncState.FAILED)
          .endGroup()
          .beginGroup()
          .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_GRID_FS)
          .or()
          .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_FILE_SYSTEM)
          .endGroup()
          .findAll().deleteAllFromRealm();
      return null;
    }).continueWith(new LogIfError());
  }

  @Override
  public RealmResults<FileUploading> queryItems(Realm realm) {
    return realm.where(FileUploading.class)
        .equalTo(FileUploading.SYNC_STATE, SyncState.NOT_SYNCED)
        .beginGroup()
        .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_GRID_FS)
        .or()
        .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_FILE_SYSTEM)
        .endGroup()
        .findAll();
  }

  @Override
  public void onUpdateResults(List<FileUploading> results) {
    if (results.isEmpty()) {
      return;
    }

    List<FileUploading> uploadingList = realmHelper.executeTransactionForReadResults(realm ->
        realm.where(FileUploading.class).equalTo(FileUploading.SYNC_STATE, SyncState.SYNCING)
            .findAll());
    if (uploadingList.size() >= 1) {
      // do not upload multiple files simultaneously
      return;
    }

    RealmUser currentUser = realmHelper.executeTransactionForRead(realm ->
        RealmUser.queryCurrentUser(realm).findFirst());
    RealmSession session = realmHelper.executeTransactionForRead(realm ->
        RealmSession.queryDefaultSession(realm).findFirst());
    if (currentUser == null || session == null) {
      return;
    }
    final String cookie = String.format("rc_uid=%s; rc_token=%s",
        currentUser.getId(), session.getToken());

    FileUploading fileUploading = results.get(0);
    final String roomId = fileUploading.getRoomId();
    final String uplId = fileUploading.getUplId();
    final String filename = fileUploading.getFilename();
    final long filesize = fileUploading.getFilesize();
    final String mimeType = fileUploading.getMimeType();
    final Uri fileUri = Uri.parse(fileUploading.getUri());
    final String store = FileUploading.STORAGE_TYPE_GRID_FS.equals(fileUploading.getStorageType())
        ? "rocketchat_uploads"
        : (FileUploading.STORAGE_TYPE_FILE_SYSTEM.equals(fileUploading.getStorageType())
            ? "fileSystem" : null);

    realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
            .put(FileUploading.ID, uplId)
            .put(FileUploading.SYNC_STATE, SyncState.SYNCING)
        )
    ).onSuccessTask(_task -> methodCall.ufsCreate(filename, filesize, mimeType, store, roomId)
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

          Response response = OkHttpHelper.INSTANCE.getClientForUploadFile().newCall(request).execute();
          if (response.isSuccessful()) {
            final JSONObject obj = new JSONObject()
                .put(FileUploading.ID, uplId)
                .put(FileUploading.UPLOADED_SIZE, offset);
            realmHelper.executeTransaction(realm ->
                realm.createOrUpdateObjectFromJson(FileUploading.class, obj));
          } else {
            return Task.forError(new Exception(response.message()));
          }
        }
      }

      return methodCall.ufsComplete(fileId, token, store);
    }).onSuccessTask(task -> methodCall.sendFileMessage(roomId, null, task.getResult())
    ).onSuccessTask(task -> realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
            .put(FileUploading.ID, uplId)
            .put(FileUploading.SYNC_STATE, SyncState.SYNCED)
            .put(FileUploading.ERROR, JSONObject.NULL)
        )
    )).continueWithTask(task -> {
      if (task.isFaulted()) {
        RCLog.w(task.getError());
        return realmHelper.executeTransaction(realm ->
            realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
                .put(FileUploading.ID, uplId)
                .put(FileUploading.SYNC_STATE, SyncState.FAILED)
                .put(FileUploading.ERROR, task.getError().getMessage())
            ));
      } else {
        return Task.forResult(null);
      }
    });
  }
}
