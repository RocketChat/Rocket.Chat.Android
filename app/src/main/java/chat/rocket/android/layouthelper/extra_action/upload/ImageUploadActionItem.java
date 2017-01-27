package chat.rocket.android.layouthelper.extra_action.upload;

import android.content.Intent;

import chat.rocket.android.R;

public class ImageUploadActionItem extends AbstractUploadActionItem {

  @Override
  public int getItemId() {
    return 10;
  }

  @Override
  protected Intent getIntentForPickFile() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    return Intent.createChooser(intent, "Select Picture to Upload");
  }

  @Override
  public int getIcon() {
    return R.drawable.ic_insert_photo_white_24dp;
  }

  @Override
  public int getTitle() {
    return R.string.image_upload_message_spec_title;
  }
}
