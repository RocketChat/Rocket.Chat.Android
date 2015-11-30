package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

public class MethodCall extends AbstractModel {
    public static final String TABLE_NAME = "method_call";

    public String params;
    public String returns;
    public long timestamp;

    public static MethodCall create(String id, JSONObject params) {
        MethodCall m = new MethodCall();
        m.id = id;
        m.params = params!=null ? params.toString() : "{}";
        m.timestamp = System.currentTimeMillis();
        m.syncstate = SyncState.NOT_SYNCED;
        return m;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    private static class DBAccessor extends AbstractModelDBAccessor<MethodCall> {

        protected DBAccessor(SQLiteDatabase db) {
            super(db, TABLE_NAME);
        }

        @Override
        protected MethodCall createModel(Cursor c) {
            return MethodCall.createFromCursor(c);
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
                            " id TEXT NOT NULL," +
                            " syncstate INTEGER NOT NULL," +
                            " params TEXT," +
                            " returns TEXT," +
                            " timestamp INTEGER);\n");

                    mDb.setTransactionSuccessful();
                }
                finally {
                    mDb.endTransaction();
                }

                updateVersion = 2;
            }
        }
    }

    public static MethodCall createFromCursor(Cursor c) {
        MethodCall m = new MethodCall();
        initID(m, c);
        m.params = c.getString(c.getColumnIndex("params"));
        m.returns = c.getString(c.getColumnIndex("returns"));
        m.timestamp = c.getLong(c.getColumnIndex("timestamp"));
        return m;
    }

    @Override
    protected ContentValues createContentValue() {
        ContentValues values = createInitContentValue();
        values.put("params",params);
        values.put("returns",returns);
        values.put("timestamp",timestamp);
        return values;
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        new DBAccessor(db).updateTable(oldVersion, newVersion);
    }

    public static void dropTable(SQLiteDatabase db) {
        new DBAccessor(db).dropTable();
    }

}
