package chat.rocket.android;


import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

public class BackgroundLooper {

  private static HandlerThread handlerThread;

  public static Looper get() {
    if (handlerThread == null) {
      handlerThread =
          new HandlerThread("BackgroundHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
      handlerThread.start();
    }

    return handlerThread.getLooper();
  }
}
