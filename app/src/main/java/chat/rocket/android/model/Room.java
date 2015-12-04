package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Room extends AbstractModel {
    public static final String TABLE_NAME = "room";

    public enum Type {
        CHANNEL("c")
        ,PRIVATE_GROUP("p")
        ,DIRECT_MESSAGE("d")

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
            throw new IllegalArgumentException("Room.Type.getType: invalid parameter: value="+value);
        }
    }

    public String name;
    public Long timestamp;
    public boolean alert;
    public Type type;
    public int unread;
    public boolean hasMore = true;

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    private static class DBAccessor extends AbstractModelDBAccessor<Room> {

        protected DBAccessor(SQLiteDatabase db) {
            super(db, TABLE_NAME);
        }

        @Override
        protected Room createModel(Cursor c) {
            return Room.createFromCursor(c);
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
                            " id TEXT UNIQUE NOT NULL," +
                            " syncstate INTEGER NOT NULL," +
                            " name TEXT," +
                            " timestamp INTEGER," +
                            " alert INTEGER," +
                            " type TEXT," +
                            " unread INTEGER," +
                            " has_more INTEGER);");

                    mDb.setTransactionSuccessful();
                }
                finally {
                    mDb.endTransaction();
                }

                updateVersion = 2;
            }
        }
    }

    public static Room createFromCursor(Cursor c) {
        Room r = new Room();
        initID(r, c);
        r.name = c.getString(c.getColumnIndex("name"));
        r.timestamp = c.getLong(c.getColumnIndex("timestamp"));
        r.alert = (c.getInt(c.getColumnIndex("alert")) != 0);
        r.type = Type.getType(c.getString(c.getColumnIndex("type")));
        r.unread = c.getInt(c.getColumnIndex("unread"));
        r.hasMore = (c.getInt(c.getColumnIndex("has_more")) != 0);
        return r;
    }

    @Override
    protected ContentValues createContentValue() {
        ContentValues values = createInitContentValue();
        values.put("name", name);
        values.put("timestamp", timestamp);
        values.put("alert",alert? 1 : 0);
        values.put("type", type.getValue());
        values.put("unread", unread);
        values.put("has_more", hasMore? 1 : 0);
        return values;
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        new DBAccessor(db).updateTable(oldVersion, newVersion);
    }

    public static void dropTable(SQLiteDatabase db) {
        new DBAccessor(db).dropTable();
    }

    public static Room get(SQLiteDatabase db, String selection, String[] selectionArgs) {
        return new DBAccessor(db).get(selection, selectionArgs,null);
    }

    public static Room get(SQLiteDatabase db, long _id) {
        return new DBAccessor(db).get(_id);
    }

    public static Room getById(SQLiteDatabase db, String id) {
        return new DBAccessor(db).getByID(id);
    }

    public void put(SQLiteDatabase db) {
        new DBAccessor(db).put(this);
    }

    public int delete(SQLiteDatabase db) {
        return new DBAccessor(db).delete(this);
    }

    public static int delete(SQLiteDatabase db, String selection, String[] selectionArgs) {
        return new DBAccessor(db).delete(selection, selectionArgs);
    }
}
