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

  public boolean hasSameDate() {
    return nextSibling != null
        && DateTime.fromEpocMs(nextSibling.getTs(), DateTime.Format.DATE)
        .equals(DateTime.fromEpocMs(target.getTs(), DateTime.Format.DATE));
  }

  public boolean hasSameUser() {
    return nextSibling != null
        && nextSibling.getU() != null && target.getU() != null
        && nextSibling.getU().get_id().equals(target.getU().get_id());
  }
}
