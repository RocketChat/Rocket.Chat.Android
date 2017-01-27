package chat.rocket.android.layouthelper.extra_action.upload;

import android.content.Intent;

import chat.rocket.android.R;

public class AudioUploadActionItem extends AbstractUploadActionItem {

  @Override
  public int getItemId() {
    return 11;
  }

  @Override
  protected Intent getIntentForPickFile() {
    Intent intent = new Intent();
    intent.setType("audio/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    return Intent.createChooser(intent, "Select Audio to Upload");
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
