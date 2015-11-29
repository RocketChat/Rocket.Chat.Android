package chat.rocket.android.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import chat.rocket.android.model.MethodCall;
import hugo.weaving.DebugLog;

/*package*/ class RocketChatOnMemoryDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = null;//on memory!
    private static final int DB_VERSION = RocketChatDatabaseHelper.DB_VERSION;

    public RocketChatOnMemoryDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, DB_VERSION);
    }

    @DebugLog
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MethodCall.updateTable(db, oldVersion, newVersion);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MethodCall.dropTable(db);
        onUpgrade(db, 0, newVersion);
    }
}
