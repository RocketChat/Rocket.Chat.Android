package chat.rocket.android.model;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

abstract class AbstractModel {
    public long _id;
    public String id;

    public AbstractModel() {
        _id = -1;
        id = "";
    }

    public final AbstractModel initID(long _id, String id) {
        this._id = _id;
        this.id = id;
        return this;
    }

    public static void initID(AbstractModel m, Cursor c) {
        m._id = c.getLong(c.getColumnIndex(BaseColumns._ID));
        m.id = c.getString(c.getColumnIndex("id"));
    }

    public boolean hasBaseID() {
        return _id >= 0;
    }

    public boolean hasID() {
        return !TextUtils.isEmpty(id);
    }

}