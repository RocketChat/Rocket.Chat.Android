package chat.rocket.android.renderer.optional;

import android.view.View;

import java.util.HashMap;

public class ViewVisibilityOptional extends HashMap<String, View> implements Optional {

  @Override
  public void onDataExists(String key) {
    if (containsKey(key)) {
      get(key).setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onNoData(String key) {
    if (containsKey(key)) {
      get(key).setVisibility(View.GONE);
    }
  }
}
