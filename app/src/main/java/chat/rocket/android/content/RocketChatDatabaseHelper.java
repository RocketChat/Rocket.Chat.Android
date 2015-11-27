package chat.rocket.android.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import chat.rocket.android.Constants;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.User;

public class RocketChatDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = Constants.LOG_TAG;
    private static final String DB_NAME="rockets.db";
    static final int DB_VERSION=2;

    public RocketChatDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, DB_VERSION);
    }

    public interface DBCallback<T> {
        T process(SQLiteDatabase db) throws Exception;
    }

    public interface DBCallbackEx<T> {
        T process(SQLiteDatabase db) throws Exception;
        void handleException(Exception e);
    }

    public static <T> T read(Context context, DBCallback<T> process) {
        T ret = null;

        SQLiteDatabase db = new RocketChatDatabaseHelper(context).getReadableDatabase();
        try {
            ret = process.process(db);
        }
        catch(Exception e){
            Log.e(TAG,"error",e);
        }
        finally {
            db.close();
        }
        return ret;
    }

    public static <T> T write(Context context, DBCallback<T> process) {
        T ret = null;

        SQLiteDatabase db = new RocketChatDatabaseHelper(context).getWritableDatabase();
        try {
            ret = process.process(db);
        }
        catch(Exception e){
            Log.e(TAG,"error",e);
        }
        finally {
            db.close();
        }
        return ret;
    }

    public static <T> T writeWithTransaction(Context context, DBCallbackEx<T> process) {
        T ret = null;

        SQLiteDatabase db = new RocketChatDatabaseHelper(context).getWritableDatabase();
        db.beginTransaction();
        try {
            ret = process.process(db);
            db.setTransactionSuccessful();
        }
        catch(Exception e){
            process.handleException(e);
        }
        finally {
            db.endTransaction();
            db.close();
        }
        return ret;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Message.updateTable(db, oldVersion, newVersion);
        Room.updateTable(db, oldVersion, newVersion);
        ServerConfig.updateTable(db, oldVersion, newVersion);
        User.updateTable(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Message.dropTable(db);
        Room.dropTable(db);
        ServerConfig.dropTable(db);
        User.dropTable(db);
        onUpgrade(db, 0, newVersion);
    }
}
