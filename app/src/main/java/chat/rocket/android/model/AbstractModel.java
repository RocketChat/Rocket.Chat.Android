package chat.rocket.android.model;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

abstract class AbstractModel {
    public long _id;
    public String id;
    public SyncState syncstate;

    public AbstractModel() {
        _id = -1;
        id = "";
        syncstate = SyncState.SYNCED;
    }

    public final AbstractModel initID(long _id, String id, SyncState syncstate) {
        this._id = _id;
        this.id = id;
        this.syncstate = syncstate;
        return this;
    }

    public static void initID(AbstractModel m, Cursor c) {
        m._id = c.getLong(c.getColumnIndex(BaseColumns._ID));
        m.id = c.getString(c.getColumnIndex("id"));
        m.syncstate = SyncState.valueOf(c.getInt(c.getColumnIndex("syncstate")));
    }

    public boolean hasBaseID() {
        return _id >= 0;
    }

    public boolean hasID() {
        return !TextUtils.isEmpty(id);
    }

}