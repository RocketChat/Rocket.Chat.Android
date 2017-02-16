package chat.rocket.android.layouthelper.chatroom;

import chat.rocket.android.helper.DateTime;
import chat.rocket.core.models.Message;

/**
 * ViewData Model for messages in chatroom.
 */
public class PairedMessage {
  public final Message target;
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
        && DateTime.fromEpocMs(nextSibling.getTimestamp(), DateTime.Format.DATE)
        .equals(DateTime.fromEpocMs(target.getTimestamp(), DateTime.Format.DATE));
  }

  /**
   * Returns true if target and nextSibling are sent by the same user.
   */
  public boolean hasSameUser() {
    return nextSibling != null
        && nextSibling.getUser() != null && target.getUser() != null
        && nextSibling.getUser().getId().equals(target.getUser().getId());
  }

  public String getId() {
    return target.getId();
  }

  @Override
  public String toString() {
    return "PairedMessage{" +
        "target=" + target +
        ", nextSibling=" + nextSibling +
        '}';
  }

  @SuppressWarnings({"PMD.ShortVariable"})
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PairedMessage that = (PairedMessage) o;

    if (!target.equals(that.target)) {
      return false;
    }
    return nextSibling != null ? nextSibling.equals(that.nextSibling) : that.nextSibling == null;

  }

  @Override
  public int hashCode() {
    int result = target.hashCode();
    result = 31 * result + (nextSibling != null ? nextSibling.hashCode() : 0);
    return result;
  }
}
