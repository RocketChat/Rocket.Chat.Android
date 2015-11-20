package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

public class User extends AbstractModel {
    public String name;

    public static class DBAccessor extends AbstractModel.DBAccessor<User> {

        @Override
        protected String getTableName() {
            return "user";
        }

        @Override
        protected ContentValues createContentValues(User instance) {
            ContentValues values = super.createContentValues(instance);
            values.put("name", instance.name);
            return values;
        }

        @Override
        protected void initializeInstance(Cursor c, @NonNull User newInstance) {
            super.initializeInstance(c, newInstance);
            newInstance.name = c.getString(c.getColumnIndex("name"));
        }

        @Override
        public void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
            int updateVersion = oldVersion;

            if(updateVersion < 1) {
                StringBuilder s = new StringBuilder();
                s.append("CREATE TABLE IF NOT EXISTS ").append(getTableName()).append("(")
                        .append(baseColumnsDef())
                        .append(",name TEXT")
                        .append(");");
                db.execSQL(s.toString());

                updateVersion = 1;
            }
        }
    }

}
