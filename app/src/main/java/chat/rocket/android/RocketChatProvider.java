package chat.rocket.android;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;

import ollie.Ollie;
import ollie.OllieProvider;

public class RocketChatProvider extends OllieProvider {
    @Override
    protected String getDatabaseName() {
        return Constants.DB_NAME;
    }

    @Override
    protected int getDatabaseVersion() {
        return Constants.DB_VERSION;
    }

    @Override
    protected Ollie.LogLevel getLogLevel() {
        return Constants.DB_LOG_LEVEL;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        enforcePermission();
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        enforcePermission();
        return super.insert(uri, values);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        enforcePermission();
        return super.update(uri, values, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        enforcePermission();
        return super.delete(uri, selection, selectionArgs);
    }

    private void enforcePermission(){
        final int uid = Binder.getCallingUid();

        if(uid == Process.myUid()) return;

        //TODO: just for debug!
        if(uid == 0 || uid == 2000 /*UID_SHELL*/) return;

        throw new SecurityException("Denied to access RocketChatProvider.");
    }
}
