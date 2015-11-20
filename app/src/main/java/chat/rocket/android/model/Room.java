package chat.rocket.android.model;

import ollie.Model;
import ollie.annotation.Column;
import ollie.annotation.Table;

@Table("room")
public class Room extends Model {
    @Column("id")
    public String _id;

    @Column("name")
    public String name;

    @Column("timestamp")
    public String timestamp;
}
