package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Room extends AbstractModel {
    public static final String TABLE_NAME = "room";

    public String name;
    public String timestamp;

    private static class DBAccessor extends AbstractModelDBAccessor<Room> {

        protected DBAccessor(SQLiteDatabase db) {
            super(db, TABLE_NAME);
        }

        @Override
        protected Room createModel(Cursor c) {
            return Room.createFromCursor(c);
        }

        @Override
        protected ContentValues createContentValue(Room instance) {
            ContentValues values = createInitContentValue(instance);
            values.put("name", instance.name);
            values.put("timestamp", instance.timestamp);
            return values;
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
                            " name TEXT," +
                            " timestamp TEXT);\n");

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
        r.timestamp = c.getString(c.getColumnIndex("timestamp"));
        return r;
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
