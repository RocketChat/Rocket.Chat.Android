package chat.rocket.android.message;

import android.content.Intent;

import chat.rocket.android.R;

public class VideoUploadMessageSpec extends FileUploadMessageSpec {

  @Override
  public ViewData getSpecificViewData() {
    return new VideoUploadViewData();
  }

  @Override
  protected Intent getIntent() {
    Intent intent = new Intent();
    intent.setType("video/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    return Intent.createChooser(intent, "Select Video to Upload");
  }

  private static class VideoUploadViewData implements MessageSpec.ViewData {
    @Override
    public int getBackgroundTint() {
      return R.color.colorAccent;
    }

    @Override
    public int getIcon() {
      return R.drawable.ic_video_call_white_24dp;
    }

    @Override
    public int getTitle() {
      return R.string.video_upload_message_spec_title;
    }
  }
}
