package chat.rocket.android.widget.helper;

import android.support.annotation.StringRes;

import java.util.HashMap;
import chat.rocket.android.widget.R;

public class IconProvider {
  private static HashMap<String, Integer> ICON_TABLE = new HashMap<String, Integer>() {
    {
      put("c", R.string.fa_hashtag);
      put("p", R.string.fa_lock);
      put("d", R.string.fa_at);
    }
  };

  @StringRes
  public static int getIcon(String type) {
    if (ICON_TABLE.containsKey(type)) {
      return ICON_TABLE.get(type);
    }

    return ICON_TABLE.get("c");
  }
}
