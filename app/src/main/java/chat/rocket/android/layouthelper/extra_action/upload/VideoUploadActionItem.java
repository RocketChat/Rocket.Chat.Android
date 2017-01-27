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
  protected DetailItemInfo[] getDetailItemList() {
    return new DetailItemInfo[] {
        new DetailItemInfo() {
          @Override
          public Intent getIntent() {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            return Intent.createChooser(intent, "Select Video to Upload");
          }

          @Override
          public int getCaption() {
            return R.string.title_pick_file;
          }

          @Override
          public int getReturnCode() {
            return RC_UPL;
          }
        },
        new DetailItemInfo() {
          @Override
          public Intent getIntent() {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); //low quality.
            return Intent.createChooser(intent, "Select video recorder");
          }

          @Override
          public int getCaption() {
            return R.string.title_record_video;
          }

          @Override
          public int getReturnCode() {
            return RC_UPL;
          }
        }
    };
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
