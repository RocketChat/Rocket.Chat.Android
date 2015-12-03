package chat.rocket.android.view;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;

/**
 * Created by yokmama on 2014/07/30.
 *
 * Referred from http://qiita.com/yokmama/items/df8f307cbd5ae796a724
 */
public abstract class CursorRecyclerViewAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    protected boolean mAutoRequery;
    protected Cursor mCursor;
    protected boolean mDataValid;
    protected Context mContext;
    protected int mRowIDColumn;
    protected ChangeObserver mChangeObserver;
    protected DataSetObserver mDataSetObserver = new MyDataSetObserver();

    public CursorRecyclerViewAdapter(Context context, Cursor cursor) {
        super();
        init(context, cursor, true);
    }

    protected void init(Context context, Cursor c, boolean autoRequery) {
        boolean cursorPresent = c != null;
        mAutoRequery = autoRequery;
        mCursor = c;
        mDataValid = cursorPresent;
        mContext = context;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow(BaseColumns._ID) : -1;
        mChangeObserver = new ChangeObserver();
        if (cursorPresent) {
            c.registerContentObserver(mChangeObserver);
            c.registerDataSetObserver(mDataSetObserver);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public void onBindViewHolder(T viewHolder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        bindView(viewHolder, mContext, position, mCursor);
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
            if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyDataSetInvalidated();
        }
        return oldCursor;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    protected void onContentChanged() {
        if (mAutoRequery && mCursor != null && !mCursor.isClosed()) {
            mDataValid = mCursor.requery();
        }
    }

    public abstract void bindView(T viewHolder, Context context, int position, Cursor cursor);

    private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }
    }

    private class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            mDataValid = false;
            notifyDataSetInvalidated();
        }
    }
}