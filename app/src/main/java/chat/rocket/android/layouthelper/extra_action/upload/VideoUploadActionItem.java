package chat.rocket.android.layouthelper.extra_action.upload;

import android.content.Intent;
import android.provider.MediaStore;

import chat.rocket.android.R;

public class VideoUploadActionItem extends AbstractUploadActionItem {

  @Override
  public int getItemId() {
    return 12;
  }

  @Override
  protected Intent getIntentForPickFile() {
    Intent intent = new Intent();
    intent.setType("video/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);

    Intent recordVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    recordVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); //low quality.

    Intent chooserIntent = Intent.createChooser(intent, "Select Video to Upload");
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { recordVideoIntent });

    return chooserIntent;
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
