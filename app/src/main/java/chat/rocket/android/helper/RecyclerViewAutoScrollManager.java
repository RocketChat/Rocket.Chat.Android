package chat.rocket.android.helper;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * workaround for bug https://code.google.com/p/android/issues/detail?id=174227
 */
public class RecyclerViewAutoScrollManager extends RecyclerView.AdapterDataObserver {

  private final LinearLayoutManager linearLayoutManager;

  public RecyclerViewAutoScrollManager(LinearLayoutManager linearLayoutManager) {
    this.linearLayoutManager = linearLayoutManager;
  }

  @Override
  public void onItemRangeInserted(int positionStart, int itemCount) {
    super.onItemRangeInserted(positionStart, itemCount);

    if (linearLayoutManager.findFirstVisibleItemPosition() <= positionStart) {
      linearLayoutManager.scrollToPosition(positionStart);
    } else {
      onAutoScrollMissed();
    }
  }

  protected void onAutoScrollMissed() {
    //do nothing by default.
  }
}
