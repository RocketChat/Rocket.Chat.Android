package chat.rocket.android.helper;

import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * ScrollListener for detecting scrolled on the bottom of the RecyclerView.
 */
public class RecyclerViewScrolledToBottomListener extends RecyclerView.OnScrollListener {

  /**
   * callback.
   */
  public interface Callback {
    void onScrolledToBottom();
  }

  private final LinearLayoutManager layoutManager;
  private final int thresholdPosition;
  private final Handler handler;
  private final Callback callback;

  /**
   * Trigger callback if the bottom item position > thresholdPosition.
   */
  public RecyclerViewScrolledToBottomListener(LinearLayoutManager layoutManager,
                                              int thresholdPosition, Callback callback) {
    this.layoutManager = layoutManager;
    this.thresholdPosition = thresholdPosition;
    this.callback = callback;

    this.handler = new Handler() {
      @Override
      public void handleMessage(android.os.Message msg) {
        onScrollEnd();
      }
    };
  }


  @Override
  public void onScrolled(RecyclerView recyclerView, int deltaX, int deltaY) {
    super.onScrolled(recyclerView, deltaX, deltaY);
    handler.removeMessages(0);
    handler.sendEmptyMessageDelayed(0, 120);
  }

  private void onScrollEnd() {
    if (layoutManager.getReverseLayout()) {
      if (layoutManager.findFirstVisibleItemPosition() <= thresholdPosition) {
        doCallback();
      }
    } else {
      if (layoutManager.findLastVisibleItemPosition() >= thresholdPosition) {
        doCallback();
      }
    }
  }

  private void doCallback() {
    if (callback != null) {
      callback.onScrolledToBottom();
    }
  }
}

