package chat.rocket.android.helper;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

@SuppressWarnings("PMD.AbstractNaming")
public abstract class LoadMoreScrollListener extends RecyclerView.OnScrollListener {

  private final LinearLayoutManager layoutManager;
  private final int loadThreshold;
  private boolean isLoading;

  /**
   * constructor. loadThreshold is better to set to 0.4 * total.
   */
  public LoadMoreScrollListener(LinearLayoutManager layoutManager, int loadThreshold) {
    this.layoutManager = layoutManager;
    this.loadThreshold = loadThreshold;
    setLoadingDone();
  }

  @Override
  public void onScrolled(RecyclerView recyclerView, int deltaX, int deltaY) {
    super.onScrolled(recyclerView, deltaX, deltaY);

    final int visibleItemCount = recyclerView.getChildCount();
    final int totalItemCount = layoutManager.getItemCount();
    final int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

    if (!isLoading
        && firstVisibleItem + visibleItemCount >= totalItemCount - loadThreshold
        && visibleItemCount < totalItemCount
        && deltaY < 0) {
      isLoading = true;
      requestMoreItem();
    }
  }

  public void setLoadingDone() {
    isLoading = false;
  }

  public abstract void requestMoreItem();
}