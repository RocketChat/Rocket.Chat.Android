package chat.rocket.android.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class SqliteUtil {
    public static long getIdWithRowId(SQLiteDatabase db, String table, long rowId) {
        long id = -1;
        if (rowId >= 0) {
            Cursor c = db.query(table, new String[]{BaseColumns._ID}, "ROWID=?", new String[]{Long.toString(rowId)}, null, null, null);
            if (c != null && c.moveToNext()) {
                id = c.getLong(0);
            }
        }
        return id;
    }
}