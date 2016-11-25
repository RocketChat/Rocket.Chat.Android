package chat.rocket.android.layouthelper.chatroom;

import chat.rocket.android.helper.DateTime;
import chat.rocket.android.model.ddp.Message;

/**
 * View Model for messages in chatroom.
 */
public class PairedMessage {
  final Message target;
  final Message nextSibling;

  public PairedMessage(Message target, Message nextSibling) {
    this.target = target;
    this.nextSibling = nextSibling;
  }

  /**
   * Returns true if target and nextSibling has the same date of timestamp.
   */
  public boolean hasSameDate() {
    return nextSibling != null
        && DateTime.fromEpocMs(nextSibling.getTs(), DateTime.Format.DATE)
        .equals(DateTime.fromEpocMs(target.getTs(), DateTime.Format.DATE));
  }

  /**
   * Returns true if target and nextSibling are sent by the same user.
   */
  public boolean hasSameUser() {
    return nextSibling != null
        && nextSibling.getU() != null && target.getU() != null
        && nextSibling.getU().get_id().equals(target.getU().get_id());
  }
}
