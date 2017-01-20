package chat.rocket.android.layouthelper.chatroom;

import bolts.Task;
import chat.rocket.android.widget.message.MessageFormLayout;

/**
 * handling MessageForm.
 */
public class MessageFormManager {
  private final MessageFormLayout messageFormLayout;
  private SendMessageCallback sendMessageCallback;

  public MessageFormManager(MessageFormLayout messageFormLayout,
                            MessageFormLayout.ExtraActionSelectionClickListener callback) {
    this.messageFormLayout = messageFormLayout;
    init(callback);
  }

  private void init(MessageFormLayout.ExtraActionSelectionClickListener listener) {
    messageFormLayout.setExtraActionSelectionClickListener(listener);
    messageFormLayout.setSubmitTextListener(this::sendMessage);
  }

  public void setSendMessageCallback(SendMessageCallback sendMessageCallback) {
    this.sendMessageCallback = sendMessageCallback;
  }

  public void clearComposingText() {
    messageFormLayout.setText("");
  }

  private void sendMessage(String message) {
    if (sendMessageCallback == null) {
      return;
    }

    messageFormLayout.setEnabled(false);
    sendMessageCallback.onSubmitText(message).onSuccess(task -> {
      clearComposingText();
      return null;
    }).continueWith(task -> {
      messageFormLayout.setEnabled(true);
      return null;
    });
  }

  public interface SendMessageCallback {
    Task<Void> onSubmitText(String messageText);
  }
}
