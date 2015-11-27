package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class User extends AbstractModel {
    public static final String TABLE_NAME = "user";

    public String roomId;
    public String name;

    private static class DBAccessor extends AbstractModelDBAccessor<User> {

        protected DBAccessor(SQLiteDatabase db) {
            super(db, TABLE_NAME);
        }

        @Override
        protected User createModel(Cursor c) {
            return createFromCursor(c);
        }

        @Override
        protected ContentValues createContentValue(User instance) {
            ContentValues values = createInitContentValue(instance);
            values.put("room_id", instance.roomId);
            values.put("name", instance.name);
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
                            " room_id TEXT," +
                            " name TEXT);\n");

                    mDb.setTransactionSuccessful();
                }
                finally {
                    mDb.endTransaction();
                }

                updateVersion = 2;
            }
        }
    }

    public static User createFromCursor(Cursor c) {
        User u = new User();
        initID(u, c);
        u.roomId = c.getString(c.getColumnIndex("room_id"));
        u.name = c.getString(c.getColumnIndex("name"));
        return u;
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        new DBAccessor(db).updateTable(oldVersion, newVersion);
    }

    public static void dropTable(SQLiteDatabase db) {
        new DBAccessor(db).dropTable();
    }


    public static User get(SQLiteDatabase db, String selection, String[] selectionArgs) {
        return new DBAccessor(db).get(selection, selectionArgs,null);
    }

    public static User get(SQLiteDatabase db, long _id) {
        return new DBAccessor(db).get(_id);
    }

    public static User getById(SQLiteDatabase db, String id) {
        return new DBAccessor(db).getByID(id);
    }

    public void put(SQLiteDatabase db) {
        new DBAccessor(db).put(this);
    }

    public int delete(SQLiteDatabase db) {
        return new DBAccessor(db).delete(this);
    }

}
