package chat.rocket.android.model;

import ollie.Model;
import ollie.annotation.Column;
import ollie.annotation.Table;

@Table("message")
public class Message extends Model{
    @Column("cid")
    public String _id;

    @Column("user_id")
    public String userId;

    @Column("content")
    public String content;

    @Column("timestamp")
    public String timestamp;
}
