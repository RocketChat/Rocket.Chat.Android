package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class User extends AbstractModel {
    public static final String TABLE_NAME = "user";
    public String name;
    public String displayName;

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public String getDisplayName() {
        return TextUtils.isEmpty(displayName)? name : displayName;
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
                            " name TEXT UNIQUE NOT NULL," +
                            " syncstate INTEGER NOT NULL," +
                            " display_name TEXT);\n");

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
        u.name = c.getString(c.getColumnIndex("name"));
        u.displayName = c.getString(c.getColumnIndex("display_name"));
        return u;
    }

    @Override
    protected ContentValues createContentValue() {
        ContentValues values = createInitContentValue();
        values.put("name", name);
        values.put("display_name", displayName);
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

    public static User getByName(SQLiteDatabase db, String name) {
        return new DBAccessor(db).get("name = '?'",new String[]{name},null);
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
