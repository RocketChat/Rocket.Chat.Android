package chat.rocket.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

abstract class AbstractModelDBAccessor<T extends AbstractModel> {
    protected SQLiteDatabase mDb;
    protected String mTableName;
    protected AbstractModelDBAccessor(SQLiteDatabase db, String tableName) {
        mDb = db;
        mTableName = tableName;
    }

    protected abstract T createModel(Cursor c);
    protected abstract void updateTable(int oldVersion, int newVersion);

    public ArrayList<T> list(String selection, String[] selectionArgs, String orderBy){
        final ArrayList<T> ret = new ArrayList<T>();
        Cursor c = mDb.query(mTableName,null,selection, selectionArgs,null,null,orderBy);
        if(c!=null) {
            while (c.moveToNext()) {
                ret.add(createModel(c));
            }
            c.close();
        }
        return ret;
    }

    public T get(String selection, String[] selectionArgs, String orderBy) {
        Cursor c = mDb.query(mTableName, null, selection, selectionArgs, null, null, orderBy);
        if(c!=null && c.moveToNext()) {
            T instance = (c.getCount() > 0) ? createModel(c) : null;
            c.close();
            return instance;
        }
        return null;
    }

    public T get(long _id){
        return get("_id=?", new String[]{Long.toString(_id)}, null);
    }

    public T getByID(String id){
        return get("id=?", new String[]{id}, null);
    }

    private void setIdWithCid(T instance){
        if (!instance.hasBaseID()){
            if(instance.hasID()){
                T origInstance = getByID(instance.id);
                if(origInstance!=null) {
                    instance.id = origInstance.id;
                }
            }
        }
    }

    public long put(T instance){
        setIdWithCid(instance);
        ContentValues values = instance.createContentValue();
        return mDb.replace(mTableName, null, values);
    }

    public int delete(String selection, String[] selectionArgs){
        return mDb.delete(mTableName,selection,selectionArgs);
    }

    public int delete(T instance){
        setIdWithCid(instance);
        return delete("_id=?", new String[]{Long.toString(instance._id)});
    }

    protected void dropTable(){
        mDb.execSQL("DROP TABLE IF EXISTS "+mTableName+";");
    }
}
