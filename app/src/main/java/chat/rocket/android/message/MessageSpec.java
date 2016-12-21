package chat.rocket.android.message;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

public abstract class MessageSpec {

  public abstract ViewData getViewData();

  public abstract void onSelect(Activity activity);

  public abstract void onSelect(Fragment fragment);

  public interface ViewData {
    @ColorRes
    int getBackgroundTint();

    @DrawableRes
    int getIcon();

    @StringRes
    int getTitle();
  }
}
