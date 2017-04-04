package chat.rocket.android.renderer;

import android.support.annotation.DrawableRes;

import chat.rocket.android.R;
import chat.rocket.android.widget.helper.UserStatusProvider;
import chat.rocket.core.models.User;

public class RocketChatUserStatusProvider implements UserStatusProvider {

  private static RocketChatUserStatusProvider INSTANCE;

  private RocketChatUserStatusProvider() {
  }

  public static RocketChatUserStatusProvider getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new RocketChatUserStatusProvider();
    }

    return INSTANCE;
  }

  @Override
  @DrawableRes
  public int getStatusResId(String status) {
    if (User.STATUS_ONLINE.equals(status)) {
      return R.drawable.userstatus_online;
    } else if (User.STATUS_AWAY.equals(status)) {
      return R.drawable.userstatus_away;
    } else if (User.STATUS_BUSY.equals(status)) {
      return R.drawable.userstatus_busy;
    } else if (User.STATUS_OFFLINE.equals(status)) {
      return R.drawable.userstatus_offline;
    }

    // unknown status is rendered as "offline" status.
    return R.drawable.userstatus_offline;
  }
}
