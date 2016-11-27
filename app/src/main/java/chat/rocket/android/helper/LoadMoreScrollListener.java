package chat.rocket.android.helper;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class LoadMoreScrollListener extends RecyclerView.OnScrollListener {

  private final LinearLayoutManager layoutManager;
  private final int loadThreshold;
  private boolean isLoading;

  public LoadMoreScrollListener(LinearLayoutManager layoutManager, int loadThreshold) {
    this.layoutManager = layoutManager;
    this.loadThreshold = loadThreshold;
    setLoadingDone();
  }

  @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    super.onScrolled(recyclerView, dx, dy);

    final int visibleItemCount = recyclerView.getChildCount();
    final int totalItemCount = layoutManager.getItemCount();
    final int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

    if (!isLoading
        && firstVisibleItem + visibleItemCount >= totalItemCount - loadThreshold
        && visibleItemCount < totalItemCount
        && dy < 0) {
      isLoading = true;
      requestMoreItem();
    }
  }

  public void setLoadingDone() {
    isLoading = false;
  }

  public abstract void requestMoreItem();
}