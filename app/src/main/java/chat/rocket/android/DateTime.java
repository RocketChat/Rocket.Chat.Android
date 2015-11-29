package chat.rocket.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateTime {
    private static final String TAG=DateTime.class.getName();

    private static final SimpleDateFormat sSimpleTimeFormat = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat sSimpleDayFormat = new SimpleDateFormat("MM/dd");
    private static final SimpleDateFormat sSimpleDayTimeFormat = new SimpleDateFormat("MM/dd HH:mm");
    private static final SimpleDateFormat sSimpleDateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public enum Format{
        DATE
        ,DAY
        ,TIME
        ,DATE_TIME
        ,DAY_TIME
        , AUTO_DAY_TIME

    }

    public static String fromEpocMs(long epocMs, Format format){
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(epocMs);

        if(format==Format.DAY) return sSimpleDayFormat.format(cal.getTime());
        if(format==Format.DATE) return sSimpleDateFormat.format(cal.getTime());
        if(format==Format.TIME) return sSimpleTimeFormat.format(cal.getTime());
        if(format==Format.DATE_TIME) return sSimpleDateTimeFormat.format(cal.getTime());
        if(format==Format.DAY_TIME) return sSimpleDayTimeFormat.format(cal.getTime());
        if(format==Format.AUTO_DAY_TIME){
            final long curTimeMs = System.currentTimeMillis();
            Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("JST"));
            cal2.setTimeInMillis(curTimeMs);

            if(cal.get(Calendar.YEAR)==cal2.get(Calendar.YEAR)
                    && cal.get(Calendar.DAY_OF_YEAR)==cal2.get(Calendar.DAY_OF_YEAR)){
                //same day.
                return sSimpleDayTimeFormat.format(cal.getTime());
            }
            else {
                return sSimpleDayFormat.format(cal.getTime());
            }
        }

        throw new IllegalArgumentException();
    }

    public static long fromDateToEpocMs(String dateString){
        try {
            Calendar cal = new GregorianCalendar();
            cal.setTime(sSimpleDateFormat.parse(dateString));
            return cal.getTimeInMillis();
        } catch (ParseException e) {
        }
        return 0;
    }

}