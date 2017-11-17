package chat.rocket.android.helper;

import android.os.Build;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import chat.rocket.android.log.RCLog;

/**
 * Utility class for converting epoch ms and date-time string.
 */
public class DateTime {
  private static final String TAG = "DateTime";

  private static final SimpleDateFormat TIME_FORMAT;
  private static final SimpleDateFormat DATE_FORMAT;
  private static final SimpleDateFormat DAY_FORMAT;
  private static final SimpleDateFormat DAY_TIME_FORMAT;
  private static final SimpleDateFormat DATE_TIME_FORMAT;

  static {
    Locale locale = Locale.getDefault();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      TIME_FORMAT = new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "HHmm"), locale);
      DATE_FORMAT =
          new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "yyyyMMdd"), locale);
      DAY_FORMAT = new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "MMdd"), locale);
      DAY_TIME_FORMAT =
          new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "MMddHHmm"), locale);
      DATE_TIME_FORMAT =
          new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "yyyyMMddHHmm"), locale);
    } else {
      TIME_FORMAT = new SimpleDateFormat("HH:mm", locale);
      DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd", locale);
      DAY_FORMAT = new SimpleDateFormat("MM/dd", locale);
      DAY_TIME_FORMAT = new SimpleDateFormat("MM/dd HH:mm", locale);
      DATE_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm", locale);
    }
  }

  /**
   * convert datetime ms to String.
   */
  public static String fromEpocMs(long epocMs, Format format) {
    Calendar cal = new GregorianCalendar();
    cal.setTimeInMillis(epocMs);

    switch (format) {
      case DAY:
        return DAY_FORMAT.format(cal.getTime());
      case DATE:
        return getDateFormat(cal.getTime());
      case TIME:
        return TIME_FORMAT.format(cal.getTime());
      case DATE_TIME:
        return DATE_TIME_FORMAT.format(cal.getTime());
      case DAY_TIME:
        return DAY_TIME_FORMAT.format(cal.getTime());
      case AUTO_DAY_TIME: {
        final long curTimeMs = System.currentTimeMillis();
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("JST"));
        cal2.setTimeInMillis(curTimeMs);

        if (cal.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
            && cal.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
          //same day.
          return DAY_TIME_FORMAT.format(cal.getTime());
        } else {
          return DAY_FORMAT.format(cal.getTime());
        }
      }
      default:
        throw new IllegalArgumentException();
    }
  }

  private static String getDateFormat(Date dateTime) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(dateTime);
      Calendar today = Calendar.getInstance();
      Calendar yesterday = Calendar.getInstance();
      yesterday.add(Calendar.DATE, -1);

      if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
        return "Today";
      } else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
        return "Yesterday";
      } else {
        return DATE_FORMAT.format(dateTime);
      }
  }

  /**
   * parse datetime string to ms.
   */
  public static long fromDateToEpocMs(String dateString) {
    try {
      Calendar cal = new GregorianCalendar();
      cal.setTime(DATE_FORMAT.parse(dateString));
      return cal.getTimeInMillis();
    } catch (ParseException exception) {
      RCLog.w(exception, "failed to parse date: %s", dateString);
    }
    return 0;
  }

  /**
   * format.
   */
  public enum Format {
    DATE, DAY, TIME, DATE_TIME, DAY_TIME, AUTO_DAY_TIME
  }
}