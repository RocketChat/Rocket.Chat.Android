package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

abstract class AbstractModel {
    public String id;
    public long updatedAt;

    abstract static class DBAccessor<T extends AbstractModel> {
        abstract protected String getTableName();

        protected final String baseColumnsDef() {
            return "id TEXT, updated_at INTEGER";
        }

        protected ContentValues createContentValues(T instance){
            ContentValues values = new ContentValues();
            values.put("id", instance.id);
            values.put("updated_at", instance.updatedAt);
            return values;
        }

        protected void initializeInstance(Cursor c, @NonNull T newInstance){
            newInstance.id = c.getString(c.getColumnIndex("id"));
            newInstance.updatedAt = c.getLong(c.getColumnIndex("updated_at"));
        }

        public abstract void updateTable(SQLiteDatabase db, int oldVersion, int newVersion);

        public void dropTable(SQLiteDatabase db){
            db.execSQL("DROP TABLE IF EXISTS "+getTableName());
        }
    }
}
