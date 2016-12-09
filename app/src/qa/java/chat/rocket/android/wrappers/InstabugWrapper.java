package chat.rocket.android.wrappers;

import android.app.Activity;
import android.app.Application;
import android.view.MotionEvent;
import com.instabug.library.Feature;
import com.instabug.library.Instabug;
import com.instabug.library.InstabugTrackingDelegate;
import com.instabug.library.invocation.InstabugInvocationEvent;

public class InstabugWrapper {

  public static void build(Application application, String apiKey) {
    new Instabug.Builder(application, apiKey)
        .setInvocationEvent(InstabugInvocationEvent.FLOATING_BUTTON)
        .setInAppMessagingState(Feature.State.DISABLED) //not available in Free plan...
        .build();
  }

  public static void wrap(MotionEvent event, Activity activity) {
    InstabugTrackingDelegate.notifyActivityGotTouchEvent(event, activity);
  }
}
