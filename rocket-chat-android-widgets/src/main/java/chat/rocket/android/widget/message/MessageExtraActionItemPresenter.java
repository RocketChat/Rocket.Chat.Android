package chat.rocket.android.widget.message;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * View Data for message extra action item.
 */
public interface MessageExtraActionItemPresenter {

  int getItemId();

  @ColorRes
  int getBackgroundTint();

  @DrawableRes
  int getIcon();

  @StringRes
  int getTitle();
}
