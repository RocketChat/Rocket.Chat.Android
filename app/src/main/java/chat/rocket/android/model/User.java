package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class User extends AbstractModel {
    public static final String TABLE_NAME = "user";

    public String displayName;
    public boolean isMe;

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    private static class DBAccessor extends AbstractModelDBAccessor<User> {

        protected DBAccessor(SQLiteDatabase db) {
            super(db, TABLE_NAME);
        }

        @Override
        protected User createModel(Cursor c) {
            return createFromCursor(c);
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
                            " display_name TEXT," +
                            " is_me INTEGER);\n");

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
        u.displayName = c.getString(c.getColumnIndex("display_name"));
        u.isMe = (c.getInt(c.getColumnIndex("is_me"))!=0);
        return u;
    }

    @Override
    protected ContentValues createContentValue() {
        ContentValues values = createInitContentValue();
        values.put("display_name", displayName);
        values.put("is_me", isMe ? 1 : 0);
        return values;
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

    public static User getMe(SQLiteDatabase db){
        return new DBAccessor(db).get("is_me = 1", null,null);
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
