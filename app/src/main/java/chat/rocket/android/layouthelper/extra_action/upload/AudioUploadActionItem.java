package chat.rocket.android.layouthelper.extra_action.upload;

import android.content.Intent;
import android.provider.MediaStore;

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

    Intent recordSoundIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);

    Intent chooserIntent = Intent.createChooser(intent, "Select Audio to Upload");
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { recordSoundIntent });

    return chooserIntent;
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
