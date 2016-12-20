package chat.rocket.android.layouthelper.chatroom;

import bolts.Task;
import chat.rocket.android.widget.message.MessageComposer;

/**
 * handling visibility of FAB-compose and MessageComposer.
 */
public class MessageComposerManager {
  private final MessageComposer messageComposer;
  private SendMessageCallback sendMessageCallback;

  public MessageComposerManager(MessageComposer messageComposer) {
    this.messageComposer = messageComposer;
    init();
  }

  private void init() {
    messageComposer.setOnActionListener(new MessageComposer.ActionListener() {
      @Override
      public void onSubmit(String message) {
        if (sendMessageCallback != null) {
          messageComposer.setEnabled(false);
          sendMessageCallback.onSubmit(message).onSuccess(task -> {
            clearComposingText();
            return null;
          }).continueWith(task -> {
            messageComposer.setEnabled(true);
            return null;
          });
        }
      }

      @Override
      public void onCancel() {
      }
    });
  }

  public void setSendMessageCallback(SendMessageCallback sendMessageCallback) {
    this.sendMessageCallback = sendMessageCallback;
  }

  public void clearComposingText() {
    messageComposer.setText("");
  }

  public interface SendMessageCallback {
    Task<Void> onSubmit(String messageText);
  }
}
