package chat.rocket.android.layouthelper.extra_action.upload;

import android.content.Intent;

import chat.rocket.android.R;

public class FileUploadActionItem extends AbstractUploadActionItem {

  @Override
  public int getItemId() {
    return 13;
  }

  @Override
  protected Intent getIntentForPickFile() {
    Intent intent = new Intent();
    intent.setType("*/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    return Intent.createChooser(intent, "Select File to Upload");
  }

  @Override
  public int getIcon() {
    return R.drawable.ic_insert_drive_file_white_24dp;
  }

  @Override
  public int getTitle() {
    return R.string.doc_upload_message_spec_title;
  }
}
