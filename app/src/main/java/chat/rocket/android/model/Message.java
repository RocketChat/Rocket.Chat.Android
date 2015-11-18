package chat.rocket.android.model;

import org.joda.time.DateTime;

public class Message {
    public String id;
    public User user;
    public String content;
    public DateTime timestamp;
}
