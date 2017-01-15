package chat.rocket.android.layouthelper.chatroom;

/**
 * manager class for showing "You have XX messages" indicator.
 */
public abstract class NewMessageIndicatorManager {
  private int count;
  private boolean onlyAlreadyShown;

  /**
   * update the number of unread message.
   */
  public void updateNewMessageCount(int count) {
    if (count > 0) {
      this.count = count;
      update();
      onlyAlreadyShown = true;
    } else {
      reset();
    }
  }

  /**
   * Should call this method when user checked new message.
   */
  public void reset() {
    count = 0;
    onlyAlreadyShown = false;
    update();
  }

  private void update() {
    if (count > 0) {
      onShowIndicator(count, onlyAlreadyShown);
    } else {
      onHideIndicator();
    }
  }

  protected abstract void onShowIndicator(int count, boolean onlyAlreadyShown);

  protected abstract void onHideIndicator();
}
