package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class Room extends AbstractModel {
    public String name;
    public List<String> usernames;
    public String timestamp;

    public static class DBAccessor extends AbstractModel.DBAccessor<Room> {

        @Override
        protected String getTableName() {
            return "room";
        }

        @Override
        protected ContentValues createContentValues(Room instance) {
            ContentValues values = super.createContentValues(instance);
            values.put("name", instance.name);
            values.put("usernames", TextUtils.join(",", instance.usernames));
            values.put("timestamp", instance.timestamp);
            return values;
        }

        @Override
        protected void initializeInstance(Cursor c, @NonNull Room newInstance) {
            super.initializeInstance(c, newInstance);
            newInstance.name = c.getString(c.getColumnIndex("name"));
            newInstance.usernames = new ArrayList<String>();
            for(String username: c.getString(c.getColumnIndex("usernames")).split(",")) {
                newInstance.usernames.add(username);
            }
            newInstance.timestamp = c.getString(c.getColumnIndex("timestamp"));
        }

        @Override
        public void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
            int updateVersion = oldVersion;

            if(updateVersion < 1) {
                StringBuilder s = new StringBuilder();
                s.append("CREATE TABLE IF NOT EXISTS ").append(getTableName()).append("(")
                        .append(baseColumnsDef())
                        .append(",name TEXT")
                        .append(",usernames TEXT")
                        .append(",timestamp TEXT")
                        .append(");");
                db.execSQL(s.toString());

                updateVersion = 1;
            }
        }
    }

}
