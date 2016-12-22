package chat.rocket.android.message;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;

public abstract class AbstractUploadMessageSpec extends AbstractMessageSpec {

  public static final int RC_UPL = 0x12;

  private ViewData viewData;

  @Override
  public ViewData getViewData() {
    if (viewData == null) {
      viewData = getSpecificViewData();
    }
    return viewData;
  }

  @Override
  public void onSelect(Activity activity) {
    activity.startActivityForResult(getIntent(), RC_UPL);
  }

  @Override
  public void onSelect(Fragment fragment) {
    fragment.startActivityForResult(getIntent(), RC_UPL);
  }

  protected abstract Intent getIntent();

  protected abstract ViewData getSpecificViewData();
}
