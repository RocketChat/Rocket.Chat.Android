package chat.rocket.android.model;

import ollie.Model;
import ollie.annotation.Column;
import ollie.annotation.Table;

@Table("user")
public class User extends Model {
    @Column("id")
    public String _id;

    @Column("room_id")
    public String roomId;

    @Column("name")
    public String name;
}
