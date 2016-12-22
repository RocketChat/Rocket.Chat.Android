package chat.rocket.android.message;

import android.content.Intent;

import chat.rocket.android.R;

public class ImageUploadMessageSpec extends AbstractUploadMessageSpec {

  @Override
  public ViewData getSpecificViewData() {
    return new ImageUploadViewData();
  }

  @Override
  protected Intent getIntent() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    return Intent.createChooser(intent, "Select Picture to Upload");
  }

  private static class ImageUploadViewData implements AbstractMessageSpec.ViewData {
    @Override
    public int getBackgroundTint() {
      return R.color.colorAccent;
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
}
