package chat.rocket.android.renderer;

import android.content.Context;
import android.view.View;

import chat.rocket.android.renderer.optional.Condition;
import chat.rocket.android.renderer.optional.Optional;

abstract class AbstractRenderer<T> {
  protected final Context context;
  protected final T object;

  protected AbstractRenderer(Context context, T object) {
    this.context = context;
    this.object = object;
  }

  protected boolean shouldHandle(View view) {
    return object != null && view != null;
  }

  protected boolean shouldHandle(View target, Condition additionalCondition, Optional optional,
                                 String key) {
    if (target == null || object == null) {
      if (optional != null) {
        optional.onNoData(key);
      }
      return false;
    }

    if (optional != null) {
      if (!additionalCondition.isOK()) {
        optional.onNoData(key);
        return false;
      } else {
        optional.onDataExists(key);
      }
    }
    return true;
  }
}
