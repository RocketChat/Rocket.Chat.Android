package chat.rocket.android.layouthelper.extra_action.upload;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import chat.rocket.android.R;
import chat.rocket.android.layouthelper.extra_action.AbstractExtraActionItem;

public abstract class AbstractUploadActionItem extends AbstractExtraActionItem {

  public static final int RC_UPL = 0x12;

  @Override
  public void handleItemSelectedOnActivity(Activity activity) {
    activity.startActivityForResult(getIntentForPickFile(), RC_UPL);
  }

  @Override
  public void handleItemSelectedOnFragment(Fragment fragment) {
    fragment.startActivityForResult(getIntentForPickFile(), RC_UPL);
  }

  protected abstract Intent getIntentForPickFile();

  @Override
  public int getBackgroundTint() {
    return R.color.colorAccent;
  }
}
