package chat.rocket.android.service.observer;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import bolts.Task;
import chat.rocket.android.api.FileUploadingHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.internal.FileUploading;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * execute file uploading and requesting sendMessage with attachment.
 */
public class FileUploadingToUrlObserver extends AbstractModelObserver<FileUploading> {
  private FileUploadingHelper methodCall;

  public FileUploadingToUrlObserver(Context context, String hostname,
                                    RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
    methodCall = new FileUploadingHelper(realmHelper);

    realmHelper.executeTransaction(realm -> {
      // resume pending operations.
      RealmResults<FileUploading> pendingUploadRequests = realm.where(FileUploading.class)
          .equalTo(FileUploading.SYNC_STATE, SyncState.SYNCING)
          .beginGroup()
          .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_S3)
          .or()
          .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_GOOGLE)
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
          .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_S3)
          .or()
          .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_GOOGLE)
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
        .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_S3)
        .or()
        .equalTo(FileUploading.STORAGE_TYPE, FileUploading.STORAGE_TYPE_GOOGLE)
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
    if (uploadingList.size() >= 3) {
      // do not upload more than 3 files simultaneously
      return;
    }

    FileUploading fileUploading = results.get(0);
    final String roomId = fileUploading.getRoomId();
    final String uplId = fileUploading.getUplId();
    final String filename = fileUploading.getFilename();
    final long filesize = fileUploading.getFilesize();
    final String mimeType = fileUploading.getMimeType();
    final Uri fileUri = Uri.parse(fileUploading.getUri());
    final String storageType = fileUploading.getStorageType();

    realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
            .put(FileUploading.ID, uplId)
            .put(FileUploading.SYNC_STATE, SyncState.SYNCING)
        )
    ).onSuccessTask(_task -> {
          if (FileUploading.STORAGE_TYPE_GOOGLE.equals(storageType)) {
            return methodCall.uploadGoogleRequest(filename, filesize, mimeType, roomId);
          } else {
            return methodCall.uploadS3Request(filename, filesize, mimeType, roomId);
          }
        }
    ).onSuccessTask(task -> {
      final JSONObject info = task.getResult();
      final String uploadUrl = info.getString("upload");
      final String downloadUrl = info.getString("download");
      final JSONArray postDataList = info.getJSONArray("postData");

      MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
          .setType(MultipartBody.FORM);

      for (int i = 0; i < postDataList.length(); i++) {
        JSONObject postData = postDataList.getJSONObject(i);
        bodyBuilder.addFormDataPart(postData.getString("name"), postData.getString("value"));
      }

      bodyBuilder.addFormDataPart("file", filename,
          new RequestBody() {
            private long numBytes = 0;

            @Override
            public MediaType contentType() {
              return MediaType.parse(mimeType);
            }

            @Override
            public long contentLength() throws IOException {
              return filesize;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
              InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
              try (Source source = Okio.source(inputStream)) {
                long readBytes;
                while ((readBytes = source.read(sink.buffer(), 8192)) > 0) {
                  numBytes += readBytes;
                  realmHelper.executeTransaction(realm ->
                      realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
                          .put(FileUploading.ID, uplId)
                          .put(FileUploading.UPLOADED_SIZE, numBytes)))
                      .continueWith(new LogIfError());
                }
              }
            }
          });

      Request request = new Request.Builder()
          .url(uploadUrl)
          .post(bodyBuilder.build())
          .build();

      Response response = OkHttpHelper.INSTANCE.getClientForUploadFile().newCall(request).execute();
      if (response.isSuccessful()) {
        return Task.forResult(downloadUrl);
      } else {
        return Task.forError(new Exception(response.message()));
      }
    }).onSuccessTask(task -> {
      String downloadUrl = task.getResult();
      String storage = FileUploading.STORAGE_TYPE_GOOGLE.equals(storageType) ? storageType : "s3";

      return methodCall.sendFileMessage(roomId, storage, new JSONObject()
          .put("_id", Uri.parse(downloadUrl).getLastPathSegment())
          .put("type", mimeType)
          .put("size", filesize)
          .put("name", filename)
          .put("url", downloadUrl)
      );
    }).onSuccessTask(task -> realmHelper.executeTransaction(realm ->
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
