package chat.rocket.android.layouthelper.chatroom;

import android.support.design.widget.FloatingActionButton;
import bolts.Task;
import chat.rocket.android.widget.message.MessageComposer;

/**
 * handling visibility of FAB-compose and MessageComposer.
 */
public class MessageComposerManager {
  public interface SendMessageCallback {
    Task<Void> onSubmit(String messageText);
  }

  public interface VisibilityChangedListener {
    void onVisibilityChanged(boolean shown);
  }

  private final FloatingActionButton fabCompose;
  private final MessageComposer messageComposer;
  private SendMessageCallback sendMessageCallback;
  private VisibilityChangedListener visibilityChangedListener;

  public MessageComposerManager(FloatingActionButton fabCompose, MessageComposer messageComposer) {
    this.fabCompose = fabCompose;
    this.messageComposer = messageComposer;
    init();
  }

  private void init() {
    fabCompose.setOnClickListener(view -> {
      setMessageComposerVisibility(true);
    });

    messageComposer.setOnActionListener(new MessageComposer.ActionListener() {
      @Override public void onSubmit(String message) {
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

      @Override public void onCancel() {
        setMessageComposerVisibility(false);
      }
    });

    setMessageComposerVisibility(false);
  }

  public void setSendMessageCallback(SendMessageCallback sendMessageCallback) {
    this.sendMessageCallback = sendMessageCallback;
  }

  public void setVisibilityChangedListener(VisibilityChangedListener listener) {
    this.visibilityChangedListener = listener;
  }

  public void clearComposingText() {
    messageComposer.setText("");
  }

  private void setMessageComposerVisibility(boolean show) {
    if (show) {
      fabCompose.hide();
      messageComposer.show(() -> {
        if (visibilityChangedListener != null) {
          visibilityChangedListener.onVisibilityChanged(true);
        }
      });
    } else {
      messageComposer.hide(() -> {
        fabCompose.show();
        if (visibilityChangedListener != null) {
          visibilityChangedListener.onVisibilityChanged(false);
        }
      });
    }
  }

  public boolean hideMessageComposerIfNeeded() {
    if (messageComposer.isShown()) {
      setMessageComposerVisibility(false);
      return true;
    }
    return false;
  }
}
