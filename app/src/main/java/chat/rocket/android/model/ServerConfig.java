package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Now we can register only 1 server (because of Ollie's restriction)
 * However, in future multi-server (seemless switching) should be supported.
 */
public class ServerConfig extends AbstractModel {
    public static final String TABLE_NAME = "server_config";

    public String hostname;
    public String account;
    public String passwd;
    public String authUserId;
    public String authToken;
    public Boolean isPrimary;

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    private static class DBAccessor extends AbstractModelDBAccessor<ServerConfig> {

        protected DBAccessor(SQLiteDatabase db) {
            super(db, TABLE_NAME);
        }

        @Override
        protected ServerConfig createModel(Cursor c) {
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
                            " hostname TEXT UNIQUE NOT NULL," +
                            " account TEXT," +
                            " passwd TEXT," +
                            " auth_user_id TEXT," +
                            " auth_token TEXT," +
                            " is_primary INTEGER);\n");

                    mDb.setTransactionSuccessful();
                }
                finally {
                    mDb.endTransaction();
                }

                updateVersion = 2;
            }
        }
    }

    public static ServerConfig createFromCursor(Cursor c) {
        ServerConfig config = new ServerConfig();
        initID(config, c);
        config.hostname = c.getString(c.getColumnIndex("hostname"));
        config.account = c.getString(c.getColumnIndex("account"));
        config.passwd = c.getString(c.getColumnIndex("passwd"));
        config.authUserId = c.getString(c.getColumnIndex("auth_user_id"));
        config.authToken = c.getString(c.getColumnIndex("auth_token"));
        config.isPrimary = (c.getInt(c.getColumnIndex("is_primary"))!=0);
        return config;
    }

    @Override
    protected ContentValues createContentValue() {
        ContentValues values = createInitContentValue();
        values.put("hostname", hostname);
        values.put("account", account);
        values.put("passwd", passwd);
        values.put("auth_user_id", authUserId);
        values.put("auth_token", authToken);
        values.put("is_primary", isPrimary);
        return values;
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        new DBAccessor(db).updateTable(oldVersion, newVersion);
    }

    public static void dropTable(SQLiteDatabase db) {
        new DBAccessor(db).dropTable();
    }

    public static ServerConfig getPrimaryConfig(SQLiteDatabase db) {
        return get(db, "is_primary = 1", null);
    }

    public static ServerConfig get(SQLiteDatabase db, String selection, String[] selectionArgs) {
        return new DBAccessor(db).get(selection, selectionArgs,null);
    }

    public static ServerConfig get(SQLiteDatabase db, long _id) {
        return new DBAccessor(db).get(_id);
    }

    public static ServerConfig getById(SQLiteDatabase db, String id) {
        return new DBAccessor(db).getByID(id);
    }

    public void put(SQLiteDatabase db) {
        new DBAccessor(db).put(this);
    }

    public int delete(SQLiteDatabase db) {
        return new DBAccessor(db).delete(this);
    }
}
