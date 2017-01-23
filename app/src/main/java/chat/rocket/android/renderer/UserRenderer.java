package chat.rocket.android.renderer;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.helper.Avatar;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ddp.User;

/**
 * Renderer for User model.
 */
public class UserRenderer extends AbstractRenderer<User> {

  public UserRenderer(Context context, User user) {
    super(context, user);
  }

  /**
   * show Avatar image.
   */
  public UserRenderer avatarInto(ImageView imageView, String hostname) {
    return avatarInto(imageView, hostname, null);
  }

  /**
   * show Avatar image - overriding the user default.
   */
  public UserRenderer avatarInto(ImageView imageView, String hostname, String avatar) {
    if (!shouldHandle(imageView)) {
      return this;
    }

    if (!TextUtils.isEmpty(object.getUsername()) || !TextUtils.isEmpty(avatar)) {
      new Avatar(hostname, object.getUsername(), avatar).into(imageView);
    }
    return this;
  }

  /**
   * show Username in textView.
   */
  public UserRenderer usernameInto(TextView textView) {
    return usernameInto(textView, null);
  }

  /**
   * show Username in textView - adding the alias first.
   */
  public UserRenderer usernameInto(TextView textView, String alias) {
    if (!shouldHandle(textView)) {
      return this;
    }

    final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
    final ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.BLACK);

    if (TextUtils.isEmpty(alias)) {
      spannableStringBuilder.append(object.getUsername());
      spannableStringBuilder.setSpan(foregroundColorSpan, 0, object.getUsername().length(),
          Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    } else {
      spannableStringBuilder.append(alias);
      spannableStringBuilder.append(" @");
      spannableStringBuilder.append(object.getUsername());
      spannableStringBuilder
          .setSpan(foregroundColorSpan, 0, alias.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }

    textView.setText(spannableStringBuilder);

    return this;
  }

  /**
   * show user's status color into imageView.
   */
  public UserRenderer statusColorInto(ImageView imageView) {
    if (!shouldHandle(imageView)) {
      return this;
    }

    String status = object.getStatus();
    if (User.STATUS_ONLINE.equals(status)) {
      imageView.setImageResource(R.drawable.userstatus_online);
    } else if (User.STATUS_AWAY.equals(status)) {
      imageView.setImageResource(R.drawable.userstatus_away);
    } else if (User.STATUS_BUSY.equals(status)) {
      imageView.setImageResource(R.drawable.userstatus_busy);
    } else if (User.STATUS_OFFLINE.equals(status)) {
      imageView.setImageResource(R.drawable.userstatus_offline);
    } else {
      // unknown status is rendered as "offline" status.
      imageView.setImageResource(R.drawable.userstatus_offline);
    }

    return this;
  }
}
