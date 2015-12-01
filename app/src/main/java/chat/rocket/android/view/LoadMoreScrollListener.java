package chat.rocket.android.view;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class LoadMoreScrollListener extends RecyclerView.OnScrollListener{

    private LinearLayoutManager mLayoutManager;
    private int mPreviousTotal;
    private boolean mIsLoading;
    private int mLoadThreshold;

    public LoadMoreScrollListener(LinearLayoutManager layoutManager, int loadThreshold){
        mLayoutManager = layoutManager;
        mLoadThreshold = loadThreshold;

        setLoadingDone();
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        final int visibleItemCount = recyclerView.getChildCount();
        final int totalItemCount = mLayoutManager.getItemCount();
        final int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

        if (!mIsLoading && firstVisibleItem + visibleItemCount >= totalItemCount - mLoadThreshold
                && visibleItemCount < totalItemCount
                && dy<0) {
            mIsLoading = true;
            requestMoreItem();
        }
    }

    public void setLoadingDone(){
        mPreviousTotal = mLayoutManager.getItemCount();
        mIsLoading = false;
    }

    public abstract void requestMoreItem();

}