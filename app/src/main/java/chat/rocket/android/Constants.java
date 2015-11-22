package chat.rocket.android;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ollie.Ollie;

public class Constants {
    public static final String LOG_TAG = "Rocket.Chat.Android";

    public static DateTimeFormatter DATETIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static final String DB_NAME = "rockets.db";
    public static final int DB_VERSION = 1;
    public static final Ollie.LogLevel DB_LOG_LEVEL = Ollie.LogLevel.BASIC;
}
