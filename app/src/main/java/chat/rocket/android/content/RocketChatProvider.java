package chat.rocket.android.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.HashMap;

import chat.rocket.android.Constants;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SqliteUtil;
import chat.rocket.android.model.User;
import chat.rocket.android.model.UserRoom;

public class RocketChatProvider extends ContentProvider {
    private static final String TAG = Constants.LOG_TAG;
    public static final String CONTENT_URI_BASE = "content://"+Constants.AUTHORITY;

    RocketChatDatabaseHelper mDBHelper;

    @Override
    public boolean onCreate() {
        mDBHelper = new RocketChatDatabaseHelper(getContext());
        return true;
    }

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int _LIST  = 100;
    private static final int _ID    = _LIST*2;
    private static final int _NEW   = _LIST*3;
    private static final HashMap<Integer, String> URI_MAP = new HashMap<Integer, String>();
    private static final String[] MODELS = {
            Message.TABLE_NAME
            , Room.TABLE_NAME
            , ServerConfig.TABLE_NAME
            , User.TABLE_NAME
            , UserRoom.TABLE_NAME
    };
    static
    {
        for(int i=0;i<MODELS.length;i++){
            final String model = MODELS[i];

            final String modelList=model+"s";
            URI_MAP.put(i+_LIST,modelList);
            sURIMatcher.addURI(Constants.AUTHORITY, modelList, i + _LIST);

            final String modelID=model+"s/#";
            URI_MAP.put(i + _ID, modelID);
            sURIMatcher.addURI(Constants.AUTHORITY, modelID, i + _ID);

            URI_MAP.put(i + _NEW, model);
            sURIMatcher.addURI(Constants.AUTHORITY, model, i + _NEW);
        }
    }
    public static String getQueryId(Uri uri){
        return uri.getPathSegments().get(1);
    }


    public static Uri getUriForInsert(String model) {
        final String uriStr = RocketChatProvider.CONTENT_URI_BASE+"/"+model;
        return Uri.parse(uriStr);
    }

    public static Uri getUriForQuery(String model) {
        return getUriForQuery(model, -1);
    }

    public static Uri getUriForQuery(String model, long id) {
        final String uriStr = (id==-1)?
                RocketChatProvider.CONTENT_URI_BASE+"/"+model+"s":
                RocketChatProvider.CONTENT_URI_BASE+"/"+model+"s/"+id;
        return Uri.parse(uriStr);
    }

    public static Uri getUriForQuery(Uri uriBase, long id) {
        return Uri.parse(uriBase.toString()+"s/"+id);
    }

    private SQLiteOpenHelper getDatabaseHelperFor(String table){
        return mDBHelper;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int match = sURIMatcher.match(uri);

        if(URI_MAP.containsKey(match)) {
            final int idx = match % _LIST;
            final String table = MODELS[idx];

            final SQLiteDatabase db = getDatabaseHelperFor(table).getReadableDatabase();
            if (match - idx == _ID) {
                String idSelection = BaseColumns._ID+"="+getQueryId(uri);
                if (!TextUtils.isEmpty(selection)) idSelection += " AND "+selection;
                return setNotifier(
                        db.query(table, projection, idSelection, selectionArgs, null, null, sortOrder),
                        uri);
            } else if (match - idx == _LIST) {
                return setNotifier(
                        db.query(table,projection, selection, selectionArgs,null,null,sortOrder),
                        uri);
            }
            // _NEW is not handled
        }

        throw new IllegalArgumentException("unsupported uri for Query:"+uri);
    }

    private Cursor setNotifier(Cursor c, Uri uri){
        c.setNotificationUri(getContext().getContentResolver(),uri);
        return c;
    }

    private void sendNotify(Uri uri){
        final int match = sURIMatcher.match(uri);
        final int i = match % _LIST;
        final int modelIdx = (match-i) / _LIST;

        if(match - i == _ID || match - i == _LIST) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sURIMatcher.match(uri);
        final int idx = match % _LIST;
        final String targetTable = MODELS[idx];

        final int i = match % _LIST;
        if(match - i == _ID || match - i == _NEW){
            return "vnd.android.cursor.item/vnd.chat.rocket.android."+targetTable;
        }
        else if(match - i == _LIST){
            return "vnd.android.cursor.dir/vnd.chat.rocket.android."+targetTable;
        }

        throw new IllegalArgumentException("unsupported uri for getType:"+uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sURIMatcher.match(uri);
        final int idx = match % _LIST;

        if(URI_MAP.containsKey(match) && (match-idx == _NEW)) {
            final String targetTable = MODELS[idx];

            final SQLiteDatabase db = getDatabaseHelperFor(targetTable).getWritableDatabase();
            final long rowId = db.insert(targetTable, null, values);

            long id = SqliteUtil.getIdWithRowId(db, targetTable, rowId);
            if (id >= 0) {
                Uri uri2 = getUriForQuery(uri,id);
                sendNotify(uri2);
                return uri2;
            } else {
                return uri;
            }
        }

        throw new IllegalArgumentException("unsupported uri for Insert:"+uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sURIMatcher.match(uri);
        final int idx = match % _LIST;

        if(URI_MAP.containsKey(match) && match-idx != _NEW) {
            final String targetTable = MODELS[idx];
            final String idSelection= (match - idx == _ID)? BaseColumns._ID+"=?" : null;

            final SQLiteDatabase db = getDatabaseHelperFor(targetTable).getWritableDatabase();
            final int count = (idSelection==null)?
                    db.delete(targetTable, selection, selectionArgs):
                    db.delete(targetTable, idSelection, new String[]{getQueryId(uri)});
            if(count>0) {
                sendNotify(uri);
            }
            return count;
        }

        throw new IllegalArgumentException("unsupported uri for Delete:"+uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sURIMatcher.match(uri);
        final int idx = match % _LIST;

        if(URI_MAP.containsKey(match) && match-idx != _NEW) {
            final String targetTable = MODELS[idx];
            final String idSelection= (match - idx == _ID)? BaseColumns._ID+"=?" : null;

            final SQLiteDatabase db = getDatabaseHelperFor(targetTable).getWritableDatabase();
            int count = db.update(targetTable, values, idSelection, new String[]{getQueryId(uri)});
            if(count>0) {
                sendNotify(uri);
            }
            return count;
        }

        throw new IllegalArgumentException("unsupported uri for Update:"+uri);
    }
    private void enforcePermission(){
        final int uid = Binder.getCallingUid();

        if(uid == Process.myUid()) return;

        //TODO: just for debug!
        if(uid == 0 || uid == 2000 /*UID_SHELL*/) return;

        throw new SecurityException("Denied to access RocketChatProvider.");
    }
}
