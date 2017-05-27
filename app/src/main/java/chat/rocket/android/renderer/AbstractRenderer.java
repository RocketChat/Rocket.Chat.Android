package chat.rocket.android.renderer;

import android.content.Context;
import android.support.annotation.Nullable;
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

  // TODO should we get rid of this simpler version of 'shouldHandle'?
  protected boolean shouldHandle(View view) {
    return object != null && view != null;
  }

  protected boolean shouldHandle(View target,
                                 @Nullable Condition additionalCondition,
                                 Optional optional,
                                 @Nullable String key) {
    if (target == null) {
      return false;
    }
    if (object == null) {
      optional.onNoData(key);
    } else {
      if (additionalCondition == null || additionalCondition.isOK()) {
        optional.onDataExists(key);
        return true;
      } else {
        optional.onNoData(key);
      }
    }
    return false;
  }
}
