package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class UserRoom extends AbstractModel {
    public static final String TABLE_NAME = "user_room";

    public String username;
    public String roomID;

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    private static class DBAccessor extends AbstractModelDBAccessor<UserRoom> {

        protected DBAccessor(SQLiteDatabase db) {
            super(db, TABLE_NAME);
        }

        @Override
        protected UserRoom createModel(Cursor c) {
            return UserRoom.createFromCursor(c);
        }

        @Override
        protected void updateTable(int oldVersion, int newVersion) {
            int updateVersion = oldVersion;

            if (updateVersion < 3) {
                mDb.beginTransaction();
                try {
                    dropTable();
                    mDb.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                            " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            " id TEXT," +
                            " syncstate INTEGER NOT NULL," +
                            " username TEXT NOT NULL," +
                            " room_id TEXT NOT NULL);");

                    mDb.setTransactionSuccessful();
                }
                finally {
                    mDb.endTransaction();
                }

                updateVersion = 2;
            }
        }
    }

    public static UserRoom createFromCursor(Cursor c) {
        UserRoom r = new UserRoom();
        initID(r, c);
        r.username = c.getString(c.getColumnIndex("username"));
        r.roomID = c.getString(c.getColumnIndex("room_id"));
        return r;
    }

    @Override
    protected ContentValues createContentValue() {
        ContentValues values = createInitContentValue();
        values.put("username", username);
        values.put("room_id", roomID);
        return values;
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        new DBAccessor(db).updateTable(oldVersion, newVersion);
    }

    public static void dropTable(SQLiteDatabase db) {
        new DBAccessor(db).dropTable();
    }

    public static ArrayList<UserRoom> list(SQLiteDatabase db, String selection, String[] selectionArgs, String orderBy) {
        return new DBAccessor(db).list(selection, selectionArgs, orderBy);
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
