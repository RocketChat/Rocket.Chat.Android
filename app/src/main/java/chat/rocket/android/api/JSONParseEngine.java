package chat.rocket.android.api;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.User;

public class JSONParseEngine {
    protected final Context mContext;
    public JSONParseEngine(Context context) {
        mContext = context;
    }

    public void parseMessage(JSONObject message) throws JSONException {
        //"_id":"X6TG3j4pNGA6HBu8Q","rid":"PTKTpXLoo9XTF62ij","msg":"hogehoge","ts":{"$date":1448132287372},"u":{"_id":"vdsT864GD3CkZPr5K","username":"test.user.2-1"}}
        final String messageId = message.getString("_id");
        Message m = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<Message>() {
            @Override
            public Message process(SQLiteDatabase db) throws Exception {
                return Message.getById(db, messageId);
            }
        });
        if(m==null){
            m = new Message();
            m.id = messageId;
        }
        m.roomId = message.getString("rid");
        m.content = message.getString("msg");
        m.timestamp = message.getJSONObject("ts").getLong("$date");
        m.type = Message.Type.getType(message.isNull("t")? "" : message.getString("t"));

        final JSONObject user = message.getJSONObject("u");
        final String userId = user.getString("_id");
        final String userName = user.getString("username");
        User _u = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<User>() {
            @Override
            public User process(SQLiteDatabase db) throws Exception {
                return User.getById(db, userId);
            }
        });
        if (_u==null) {
            final User u = new User();
            u.id = userId;
            u.name = userName;
            RocketChatDatabaseHelper.write(mContext, new RocketChatDatabaseHelper.DBCallback<Object>() {
                @Override
                public Object process(SQLiteDatabase db) throws Exception {
                    u.put(db);
                    return null;
                }
            });
        }

        m.userId = userId;

        if(!message.isNull("urls")) {
            m.urls = message.getJSONArray("urls").toString();
        }
        else m.urls = "[]";
        m.extras = "{}";
        m.putByContentProvider(mContext);
    }

    public void parseRoom(JSONObject room) throws JSONException {
        final String roomID = room.getString("rid");

        Room r = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<Room>() {
            @Override
            public Room process(SQLiteDatabase db) throws Exception {
                return Room.getById(db, roomID);
            }
        });
        if(r==null) {
            r = new Room();
            r.id = roomID;
        }

        if(!room.isNull("name")) r.name = room.getString("name");
        if(!room.isNull("ts")) r.timestamp = room.getJSONObject("ts").getLong("$date");
        if(!room.isNull("t")) r.type = Room.Type.getType(room.getString("t"));
        if(!room.isNull("alert")) r.alert = room.getBoolean("alert");
        if(!room.isNull("unread")) r.unread = room.getInt("unread");
        r.putByContentProvider(mContext);
    }
}
