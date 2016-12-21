package chat.rocket.android.message;

import android.content.Intent;

import chat.rocket.android.R;

public class AudioUploadMessageSpec extends FileUploadMessageSpec {

  @Override
  protected Intent getIntent() {
    Intent intent = new Intent();
    intent.setType("audio/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    return Intent.createChooser(intent, "Select Audio to Upload");
  }

  @Override
  public ViewData getSpecificViewData() {
    return new AudioUploadViewData();
  }

  private static class AudioUploadViewData implements MessageSpec.ViewData {
    @Override
    public int getBackgroundTint() {
      return R.color.colorAccent;
    }

    @Override
    public int getIcon() {
      return R.drawable.ic_audiotrack_white_24dp;
    }

    @Override
    public int getTitle() {
      return R.string.audio_upload_message_spec_title;
    }
  }
}
