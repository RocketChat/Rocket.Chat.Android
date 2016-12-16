package chat.rocket.android.model.internal;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * holding statuses for uploading file.
 */
public class FileUploading extends RealmObject {
  public static final String STORAGE_TYPE_S3 = "AmazonS3";
  public static final String STORAGE_TYPE_GRID_FS = "GridFS";
  public static final String STORAGE_TYPE_FILE_SYSTEM = "FileSystem";

  @PrimaryKey private String uplId;
  private int syncstate;
  private String storageType;
  private String uri;
  private String filename;
  private long filesize;
  private String mimeType;
  private String roomId;

  private long uploadedSize;
  private String error;

  public String getUplId() {
    return uplId;
  }

  public void setUplId(String uplId) {
    this.uplId = uplId;
  }

  public int getSyncstate() {
    return syncstate;
  }

  public void setSyncstate(int syncstate) {
    this.syncstate = syncstate;
  }

  public String getStorageType() {
    return storageType;
  }

  public void setStorageType(String storageType) {
    this.storageType = storageType;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public long getFilesize() {
    return filesize;
  }

  public void setFilesize(long filesize) {
    this.filesize = filesize;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public long getUploadedSize() {
    return uploadedSize;
  }

  public void setUploadedSize(long uploadedSize) {
    this.uploadedSize = uploadedSize;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
