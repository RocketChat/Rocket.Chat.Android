package chat.rocket.android.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import timber.log.Timber;

/**
 * Utility class for converting epoch ms and date-time string.
 */
public class DateTime {
  private static final String TAG = DateTime.class.getName();

  private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
  private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("MM/dd");
  private static final SimpleDateFormat DAY_TIME_FORMAT = new SimpleDateFormat("MM/dd HH:mm");
  private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

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
        return DATE_FORMAT.format(cal.getTime());
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

  /**
   * parse datetime string to ms.
   */
  public static long fromDateToEpocMs(String dateString) {
    try {
      Calendar cal = new GregorianCalendar();
      cal.setTime(DATE_FORMAT.parse(dateString));
      return cal.getTimeInMillis();
    } catch (ParseException exception) {
      Timber.w(exception, "failed to parse date: %s", dateString);
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