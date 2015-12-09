package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.Constants;

public class ServerConfig extends AbstractModel {
    public static final String TABLE_NAME = "server_config";

    public enum AuthType {
        EMAIL("email")
        ,GITHUB("github")
        ,TWITTER("twitter")

        ,UNSPECIFIED("")
        ;//------------

        private String value;
        AuthType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static AuthType getType(String value) {
            for(AuthType t :AuthType.values()){
                if(t.value.equals(value)) return t;
            }
            throw new IllegalArgumentException("AuthType.getType: invalid parameter: value="+value);
        }
    }

    public String hostname;
    public String account;
    public AuthType authType = AuthType.UNSPECIFIED;
    public String password;
    public String authUserId;
    public String authToken;
    public Boolean isPrimary;
    public String oauthProviders;

    public JSONObject getOAuthProviders() {
        if (TextUtils.isEmpty(oauthProviders)) return new JSONObject();
        try {
            return new JSONObject(oauthProviders);
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "error", e);
            return new JSONObject();
        }
    }
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
                            " auth_type TEXT," +
                            " password TEXT," +
                            " auth_user_id TEXT," +
                            " auth_token TEXT," +
                            " is_primary INTEGER," +
                            " oauth_prividers TEXT);\n");

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
        ServerConfig conf = new ServerConfig();
        initID(conf, c);
        conf.hostname = c.getString(c.getColumnIndex("hostname"));
        conf.account = c.getString(c.getColumnIndex("account"));
        conf.authType = AuthType.getType(c.getString(c.getColumnIndex("auth_type")));
        conf.password = c.getString(c.getColumnIndex("password"));
        conf.authUserId = c.getString(c.getColumnIndex("auth_user_id"));
        conf.authToken = c.getString(c.getColumnIndex("auth_token"));
        conf.isPrimary = (c.getInt(c.getColumnIndex("is_primary"))!=0);
        conf.oauthProviders = c.getString(c.getColumnIndex("oauth_prividers"));
        return conf;
    }

    @Override
    protected ContentValues createContentValue() {
        ContentValues values = createInitContentValue();
        values.put("hostname", hostname);
        values.put("account", account);
        values.put("auth_type", authType.getValue());
        values.put("password", password);
        values.put("auth_user_id", authUserId);
        values.put("auth_token", authToken);
        values.put("is_primary", isPrimary);
        values.put("oauth_prividers", oauthProviders);
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

    public static int delete(SQLiteDatabase db, String selection, String[] selectionArgs) {
        return new DBAccessor(db).delete(selection, selectionArgs);
    }
}
