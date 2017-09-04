package chat.rocket.android.layouthelper.chatroom;

import chat.rocket.android.widget.message.MessageFormLayout;

/**
 * Handles MessageForm.
 */
public class MessageFormManager {
  private final MessageFormLayout messageFormLayout;
  private SendMessageCallback sendMessageCallback;

  public MessageFormManager(MessageFormLayout messageFormLayout, MessageFormLayout.ExtraActionSelectionClickListener callback) {
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

  public void onMessageSend() {
    clearComposingText();
  }

  public void setEditMessage(String message) {
    clearComposingText();
    messageFormLayout.setText(message);
  }

  public void clearComposingText() {
    messageFormLayout.setText("");
  }

  private void sendMessage(String message) {
    if (sendMessageCallback == null) {
      return;
    }
    sendMessageCallback.onSubmitText(message);
  }

  public interface SendMessageCallback {
    void onSubmitText(String messageText);
  }
}