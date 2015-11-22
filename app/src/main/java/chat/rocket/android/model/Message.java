package chat.rocket.android.model;

import android.database.Cursor;
import android.provider.BaseColumns;

import ollie.Model;
import ollie.annotation.Column;
import ollie.annotation.Table;

@Table("message")
public class Message extends Model{
    @Column("cid")
    public String _id;

    @Column("room_id")
    public String roomId;

    @Column("user_id")
    public String userId;

    @Column("content")
    public String content;

    @Column("timestamp")
    public String timestamp;

    public static Message fromCursor(Cursor c) {
        Message m = new Message();
        m.id = c.getLong(c.getColumnIndex(BaseColumns._ID));
        m._id = c.getString(c.getColumnIndex("cid"));
        m.roomId = c.getString(c.getColumnIndex("room_id"));
        m.userId = c.getString(c.getColumnIndex("user_id"));
        m.content = c.getString(c.getColumnIndex("content"));
        m.timestamp = c.getString(c.getColumnIndex("timestamp"));
        return m;
    }
}
