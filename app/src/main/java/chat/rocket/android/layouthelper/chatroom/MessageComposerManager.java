package chat.rocket.android.layouthelper.chatroom;

import bolts.Task;
import chat.rocket.android.widget.message.MessageComposer;

/**
 * handling visibility of FAB-compose and MessageComposer.
 */
public class MessageComposerManager {
  private final MessageComposer messageComposer;
  private SendMessageCallback sendMessageCallback;
  private ExtrasPickerListener extrasPickerListener;

  public MessageComposerManager(MessageComposer messageComposer) {
    this.messageComposer = messageComposer;
    init();
  }

  private void init() {
    messageComposer.setOnActionListener(new MessageComposer.ActionListener() {
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
    messageComposer.setText("");
  }

  private void sendMessage(String message) {
    if (sendMessageCallback == null) {
      return;
    }

    messageComposer.setEnabled(false);
    sendMessageCallback.onSubmit(message).onSuccess(task -> {
      clearComposingText();
      return null;
    }).continueWith(task -> {
      messageComposer.setEnabled(true);
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
