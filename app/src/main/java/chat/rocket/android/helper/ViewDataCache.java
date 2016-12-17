package chat.rocket.android.helper;

import android.view.View;

/**
 * save String to setTag.
 */
public class ViewDataCache {

  /**
   * stores str if not stored. returns true if already stored.
   */
  public static boolean isStored(String str, View view) {
    if (view.getTag() != null && view.getTag() instanceof String
        && ((String) view.getTag()).equals(str)) {
      return true;
    }
    view.setTag(str);
    return false;
  }
}
