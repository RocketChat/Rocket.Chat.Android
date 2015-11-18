package chat.rocket.android.model;

import org.joda.time.DateTime;

import java.util.List;

public class Room {
    public String id;
    public String name;
    public List<String> usernames;
    public DateTime timestamp;
}
