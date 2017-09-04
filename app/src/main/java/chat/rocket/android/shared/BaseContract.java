package chat.rocket.android.shared;

import android.support.annotation.NonNull;

public interface BaseContract {

  interface View {
  }

  interface Presenter<T extends View> {
    void bindView(@NonNull T view);

    void release();
  }
}
