package chat.rocket.android;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Constants {
    public static final String LOG_TAG = "Rocket.Chat.Android";

    public static final String AUTHORITY = "chat.rocket.android";

    public static DateTimeFormatter DATETIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
}
