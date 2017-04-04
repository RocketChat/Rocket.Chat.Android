package chat.rocket.android.widget.helper;

import android.support.annotation.DrawableRes;

public interface UserStatusProvider {

  @DrawableRes
  int getStatusResId(String status);
}
