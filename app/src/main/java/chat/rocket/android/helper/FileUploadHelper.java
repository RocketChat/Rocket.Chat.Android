package chat.rocket.android.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import chat.rocket.android.log.RCLog;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmPublicSetting;
import chat.rocket.persistence.realm.models.internal.FileUploading;

/**
 * utility class for uploading file.
 */
public class FileUploadHelper {

  private final Context context;
  private final RealmHelper realmHelper;

  public FileUploadHelper(Context context, RealmHelper realmHelper) {
    this.context = context;
    this.realmHelper = realmHelper;
  }

  /**
   * requestUploading file. returns id for observing progress.
   */
  public
  @Nullable
  String requestUploading(String roomId, Uri uri) {
    try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        String filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        long filesize = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
        String mimeType = context.getContentResolver().getType(uri);
        return insertRequestRecord(roomId, uri, filename, filesize, mimeType);
      } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
        String filename = uri.getLastPathSegment();
        long filesize = detectFileSizeFor(uri);
        String mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
        return insertRequestRecord(roomId, uri, filename, filesize, mimeType);
      }
    }
    return null;
  }

  private String insertRequestRecord(String roomId,
                                     Uri uri, String filename, long filesize, String mimeType) {
    final String uplId = UUID.randomUUID().toString();
    final String storageType =
        RealmPublicSetting.getString(realmHelper, "FileUpload_Storage_Type", null);

    realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(FileUploading.class, new JSONObject()
            .put(FileUploading.ID, uplId)
            .put(FileUploading.SYNC_STATE, SyncState.NOT_SYNCED)
            .put(FileUploading.STORAGE_TYPE,
                TextUtils.isEmpty(storageType) ? JSONObject.NULL : storageType)
            .put(FileUploading.URI, uri.toString())
            .put(FileUploading.FILENAME, filename)
            .put(FileUploading.FILE_SIZE, filesize)
            .put(FileUploading.MIME_TYPE, mimeType)
            .put(FileUploading.ROOM_ID, roomId)
            .put(FileUploading.ERROR, JSONObject.NULL)
        )
    ).continueWith(new LogIfError());
    return uplId;
  }

  private long detectFileSizeFor(Uri uri) {
    ParcelFileDescriptor pfd = null;
    try {
      pfd = context.getContentResolver().openFileDescriptor(uri, "r");
      return Math.max(pfd.getStatSize(), 0);
    } catch (final FileNotFoundException exception) {
      RCLog.w(exception);
    } finally {
      if (pfd != null) {
        try {
          pfd.close();
        } catch (final IOException e) {
          // Do nothing.
        }
      }
    }
    return -1;
  }
}
