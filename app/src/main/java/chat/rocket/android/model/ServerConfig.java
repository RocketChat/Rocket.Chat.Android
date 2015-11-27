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
    public String authUserId;
    public String authToken;
    public Boolean isPrimary;

    private static class DBAccessor extends AbstractModelDBAccessor<ServerConfig> {

        protected DBAccessor(SQLiteDatabase db) {
            super(db, TABLE_NAME);
        }

        @Override
        protected ServerConfig createModel(Cursor c) {
            ServerConfig config = new ServerConfig();
            initID(config, c);
            config.hostname = c.getString(c.getColumnIndex("hostname"));
            config.account = c.getString(c.getColumnIndex("account"));
            config.authUserId = c.getString(c.getColumnIndex("auth_user_id"));
            config.authToken = c.getString(c.getColumnIndex("auth_token"));
            config.isPrimary = (c.getInt(c.getColumnIndex("is_primary"))!=0);
            return config;
        }

        @Override
        protected ContentValues createContentValue(ServerConfig instance) {
            ContentValues values = createInitContentValue(instance);
            values.put("hostname", instance.hostname);
            values.put("account", instance.account);
            values.put("auth_user_id", instance.authUserId);
            values.put("auth_token", instance.authToken);
            values.put("is_primary", instance.isPrimary);
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
                            " hostname TEXT UNIQUE NOT NULL," +
                            " account TEXT," +
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

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        new DBAccessor(db).updateTable(oldVersion, newVersion);
    }

    public static void dropTable(SQLiteDatabase db) {
        new DBAccessor(db).dropTable();
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
