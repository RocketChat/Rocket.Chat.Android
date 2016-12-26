package chat.rocket.android.layouthelper.extra_action;

import android.app.Activity;
import android.support.v4.app.Fragment;

public interface MessageExtraActionBehavior {

  void handleItemSelectedOnActivity(Activity activity);

  void handleItemSelectedOnFragment(Fragment fragment);
}
