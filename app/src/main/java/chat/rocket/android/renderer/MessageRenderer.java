package chat.rocket.android.renderer;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import chat.rocket.android.R;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.widget.message.RocketChatMessageLayout;

/**
 * Renderer for Message model.
 */
public class MessageRenderer extends AbstractRenderer<Message> {

  private final UserRenderer userRenderer;

  public MessageRenderer(Context context, Message message) {
    super(context, message);
    userRenderer = new UserRenderer(context, message.getU());
  }

  /**
   * show Avatar image.
   */
  public MessageRenderer avatarInto(ImageView imageView, String hostname) {
    switch (object.getSyncstate()) {
      case SyncState.FAILED:
        imageView.setImageResource(R.drawable.ic_error_outline_black_24dp);
        break;
      default:
        userRenderer.avatarInto(imageView, hostname);
        break;
    }
    return this;
  }

  /**
   * show Username in textView.
   */
  public MessageRenderer usernameInto(TextView textView) {
    userRenderer.usernameInto(textView);
    return this;
  }

  /**
   * show timestamp in textView.
   */
  public MessageRenderer timestampInto(TextView textView) {
    if (!shouldHandle(textView)) {
      return this;
    }

    switch (object.getSyncstate()) {
      case SyncState.NOT_SYNCED:
      case SyncState.SYNCING:
        textView.setText(R.string.sending);
        break;
      default:
        textView.setText(DateTime.fromEpocMs(object.getTs(), DateTime.Format.TIME));
        break;
    }

    return this;
  }

  /**
   * show body in RocketChatMessageLayout.
   */
  public MessageRenderer bodyInto(RocketChatMessageLayout rocketChatMessageLayout) {
    if (!shouldHandle(rocketChatMessageLayout)) {
      return this;
    }

    rocketChatMessageLayout.setText(object.getMsg());

    return this;
  }
}
