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
  protected DetailItemInfo[] getDetailItemList() {
    return new DetailItemInfo[] {
        new DetailItemInfo() {
          @Override
          public Intent getIntent() {
            Intent intent = new Intent();
            intent.setType("audio/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            return Intent.createChooser(intent, "Select Audio to Upload");
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
          Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
          return Intent.createChooser(intent, "Select audio recorder");
        }

        @Override
        public int getCaption() {
          return R.string.title_record_audio;
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
    return R.drawable.ic_audiotrack_white_24dp;
  }

  @Override
  public int getTitle() {
    return R.string.audio_upload_message_spec_title;
  }
}
