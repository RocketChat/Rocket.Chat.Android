package chat.rocket.android.layouthelper.chatroom;

import bolts.Task;
import chat.rocket.android.widget.message.MessageFormLayout;

/**
 * handling MessageForm.
 */
public class MessageFormManager {
  private final MessageFormLayout messageFormLayout;
  private SendMessageCallback sendMessageCallback;
  private ExtrasPickerListener extrasPickerListener;

  public MessageFormManager(MessageFormLayout messageFormLayout) {
    this.messageFormLayout = messageFormLayout;
    init();
  }

  private void init() {
    messageFormLayout.setOnActionListener(new MessageFormLayout.ActionListener() {
      @Override
      public void onSubmit(String message) {
        sendMessage(message);
      }

      @Override
      public void onExtra() {
        openExtras();
      }

      @Override
      public void onCancel() {
      }
    });
  }

  public void setSendMessageCallback(SendMessageCallback sendMessageCallback) {
    this.sendMessageCallback = sendMessageCallback;
  }

  public void setExtrasPickerListener(ExtrasPickerListener listener) {
    extrasPickerListener = listener;
  }

  public void clearComposingText() {
    messageFormLayout.setText("");
  }

  private void sendMessage(String message) {
    if (sendMessageCallback == null) {
      return;
    }

    messageFormLayout.setEnabled(false);
    sendMessageCallback.onSubmit(message).onSuccess(task -> {
      clearComposingText();
      return null;
    }).continueWith(task -> {
      messageFormLayout.setEnabled(true);
      return null;
    });
  }

  private void openExtras() {
    if (extrasPickerListener == null) {
      return;
    }

    extrasPickerListener.onOpen();
  }

  public interface SendMessageCallback {
    Task<Void> onSubmit(String messageText);
  }

  public interface ExtrasPickerListener {
    void onOpen();
  }
}
