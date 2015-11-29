package chat.rocket.android.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import chat.rocket.android.content.RocketChatProvider;

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
        m._id = c.getLong(c.getColumnIndex("_id"));
        m.id = c.getString(c.getColumnIndex("id"));
        m.syncstate = SyncState.valueOf(c.getInt(c.getColumnIndex("syncstate")));
    }

    protected ContentValues createInitContentValue() {
        final ContentValues values = new ContentValues();
        if (hasBaseID()) values.put("_id", _id);
        if (hasID()) values.put("id", id);
        values.put("syncstate", syncstate.getValue());
        return values;
    }

    abstract protected ContentValues createContentValue();
    abstract protected String getTableName();

    public boolean hasBaseID() {
        return _id >= 0;
    }

    public boolean hasID() {
        return !TextUtils.isEmpty(id);
    }

    public Uri putByContentProvider(Context context) {
        if (hasBaseID()) {
            context.getContentResolver().update(RocketChatProvider.getUriForQuery(getTableName(), _id)
                    , createContentValue(), "_id=?", new String[]{Long.toString(_id)});
            return null;
        }
        else {
            return context.getContentResolver().insert(RocketChatProvider.getUriForInsert(getTableName())
                    , createContentValue());
        }
    }

    public void deleteByContentProvider(Context context) {
        if (hasBaseID()) {
            context.getContentResolver().delete(RocketChatProvider.getUriForQuery(getTableName(), _id)
                    , "_id=?", new String[]{Long.toString(_id)});
        }
    }

}