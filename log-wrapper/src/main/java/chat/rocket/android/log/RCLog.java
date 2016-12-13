package chat.rocket.android.log;

import android.util.Log;

public class RCLog {
  public static void d(String log, Object... args) {
    Log.d(getTag(), String.format(log, args));
  }

  public static void d(Throwable throwable) {
    Log.d(getTag(), throwable.getMessage(), throwable);
  }

  public static void d(Throwable throwable, String log, Object... args) {
    Log.d(getTag(), String.format(log, args), throwable);
  }

  public static void w(String log, Object... args) {
    Log.w(getTag(), String.format(log, args));
  }

  public static void w(Throwable throwable) {
    Log.w(getTag(), throwable.getMessage(), throwable);
  }

  public static void w(Throwable throwable, String log, Object... args) {
    Log.w(getTag(), String.format(log, args), throwable);
  }

  public static void e(String log, Object... args) {
    Log.e(getTag(), String.format(log, args));
  }

  public static void e(Throwable throwable) {
    Log.e(getTag(), throwable.getMessage(), throwable);
  }

  public static void e(Throwable throwable, String log, Object... args) {
    Log.e(getTag(), String.format(log, args), throwable);
  }

  private static String getTag() {
    StackTraceElement[] elements = Thread.currentThread().getStackTrace();
    if (elements.length >= 5) {
      return getSimpleName(elements[4].getClassName());
    } else {
      return "Rocket.Chat";
    }
  }

  private static String getSimpleName(String className) {
    int idx = className.lastIndexOf(".");
    return className.substring(idx + 1);
  }
}
