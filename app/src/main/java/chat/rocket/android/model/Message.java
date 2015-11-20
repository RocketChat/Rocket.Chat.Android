package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

public class Message extends AbstractModel {
    public String userId;
    public String content;
    public String timestamp;

    public static class DBAccessor extends AbstractModel.DBAccessor<Message> {

        @Override
        protected String getTableName() {
            return "message";
        }

        @Override
        protected ContentValues createContentValues(Message instance) {
            ContentValues values = super.createContentValues(instance);
            values.put("user_id", instance.userId);
            values.put("content", instance.content);
            values.put("timestamp", instance.timestamp);
            return values;
        }

        @Override
        protected void initializeInstance(Cursor c, @NonNull Message newInstance) {
            super.initializeInstance(c, newInstance);
            newInstance.userId = c.getString(c.getColumnIndex("user_id"));
            newInstance.content = c.getString(c.getColumnIndex("content"));
        }

        @Override
        public void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
            int updateVersion = oldVersion;

            if(updateVersion < 1) {
                StringBuilder s = new StringBuilder();
                s.append("CREATE TABLE IF NOT EXISTS ").append(getTableName()).append("(")
                        .append(baseColumnsDef())
                        .append(",user_id TEXT")
                        .append(",content TEXT")
                        .append(",timestamp TEXT")
                        .append(");");
                db.execSQL(s.toString());

                updateVersion = 1;
            }
        }
    }
}
