package chat.rocket.android.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import chat.rocket.android.content.RocketChatProvider;

public class Message extends AbstractModel{
    public static final String TABLE_NAME = "message";

    public enum Type {
        ROOM_NAME_CHANGED("r")
        ,USER_ADDED("au")
        ,USER_REMOVED("ru")
        ,USER_JOINED("uj")
        ,USER_LEFT("ul")
        ,WELCOME("wm")
        ,MESSAGE_REMOVED("rm")
        ,UNSPECIFIED("")

        ;//------------

        private String value;
        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Type getType(String value) {
            for(Type t :Type.values()){
                if(t.value.equals(value)) return t;
            }
            return UNSPECIFIED;
        }
    }

    public String roomId;
    public String userId;
    public Type type = Type.UNSPECIFIED;
    public String content;
    public Long timestamp;

    /**
     * REMARK: JSON Array!
     *
     * [
     *   {
     *     "url": "https://yi01rocket.herokuapp.com/ufs/rocketchat_uploads/sTdxKn8onYR5nAWr9.png",
     *     "meta": null,
     *     "headers": {
     *       "contentType": "image/png",
     *       "contentLength": "16509"
     *     },
     *     "parsedUrl": {
     *       "host": "yi01rocket.herokuapp.com",
     *       "hash": null,
     *       "pathname": "/ufs/rocketchat_uploads/sTdxKn8onYR5nAWr9.png",
     *       "protocol": "https:",
     *       "port": null,
     *       "query": null,
     *       "search": null
     *     }
     *   },
     *   {
     *     "url": "https://yi01rocket.herokuapp.com/ufs/rocketchat_uploads/sTdxKn8onYR5nAWr9.png",
     *     "meta": null,
     *     "headers": {
     *       "contentType": "image/png",
     *       "contentLength": "16509"
     *     },
     *     "parsedUrl": {
     *       "host": "yi01rocket.herokuapp.com",
     *       "hash": null,
     *       "pathname": "/ufs/rocketchat_uploads/sTdxKn8onYR5nAWr9.png",
     *       "protocol": "https:",
     *       "port": null,
     *       "query": null,
     *       "search": null
     *     }
     *   }
     * ]
     *
     */
    public String urls;
    public String extras;

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    private static class DBAccessor extends AbstractModelDBAccessor<Message> {

        protected DBAccessor(SQLiteDatabase db) {
            super(db, TABLE_NAME);
        }

        @Override
        protected Message createModel(Cursor c) {
            return Message.createFromCursor(c);
        }

        @Override
        protected void updateTable(int oldVersion, int newVersion) {
            int updateVersion = oldVersion;

            if (updateVersion < 2) {
                mDb.beginTransaction();
                try {
                    dropTable();
                    mDb.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                            " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            " id TEXT UNIQUE," +
                            " syncstate INTEGER NOT NULL," +
                            " room_id TEXT," +
                            " user_id TEXT," +
                            " type TEXT," +
                            " content TEXT," +
                            " timestamp INTEGER," +
                            " urls TEXT," +
                            " extras TEXT);");

                    mDb.setTransactionSuccessful();
                }
                finally {
                    mDb.endTransaction();
                }

                updateVersion = 2;
            }
        }
    }

    public static Message createFromCursor(Cursor c) {
        Message m = new Message();
        initID(m, c);
        m.roomId = c.getString(c.getColumnIndex("room_id"));
        m.userId = c.getString(c.getColumnIndex("user_id"));
        m.type = Type.getType(c.getString(c.getColumnIndex("type")));
        m.content = c.getString(c.getColumnIndex("content"));
        m.timestamp = c.getLong(c.getColumnIndex("timestamp"));
        m.urls = c.getString(c.getColumnIndex("urls"));
        m.extras = c.getString(c.getColumnIndex("extras"));
        return m;
    }

    @Override
    protected ContentValues createContentValue() {
        ContentValues values = createInitContentValue();
        values.put("room_id", roomId);
        values.put("user_id", userId);
        values.put("type", type.getValue());
        values.put("content", content);
        values.put("timestamp", timestamp);
        values.put("urls", urls);
        values.put("extras", extras);
        return values;
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        new DBAccessor(db).updateTable(oldVersion, newVersion);
    }

    public static void dropTable(SQLiteDatabase db) {
        new DBAccessor(db).dropTable();
    }

    public static Message get(SQLiteDatabase db, String selection, String[] selectionArgs, String orderBy) {
        return new DBAccessor(db).get(selection, selectionArgs, orderBy);
    }

    public static Message get(SQLiteDatabase db, long _id) {
        return new DBAccessor(db).get(_id);
    }

    public static Message getById(SQLiteDatabase db, String id) {
        return new DBAccessor(db).getByID(id);
    }

    public long put(SQLiteDatabase db) {
        return new DBAccessor(db).put(this);
    }

    public int delete(SQLiteDatabase db) {
        return new DBAccessor(db).delete(this);
    }

    public static int delete(SQLiteDatabase db, String selection, String[] selectionArgs) {
        return new DBAccessor(db).delete(selection, selectionArgs);
    }

    public static int deleteByContentProvider(Context context, String selection, String[] selectionArgs) {
        return context.getContentResolver().delete(RocketChatProvider.getUriForQuery(TABLE_NAME), selection, selectionArgs);
    }
}
