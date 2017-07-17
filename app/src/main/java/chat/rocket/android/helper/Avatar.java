package chat.rocket.android.helper;

import chat.rocket.android.log.RCLog;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.RocketChatAvatar;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Helper for rendering user avatar image.
 */
public class Avatar {
  private final AbsoluteUrl absoluteUrl;
  private final String username;

  public Avatar(AbsoluteUrl absoluteUrl, String username) {
    this.absoluteUrl = absoluteUrl;
    this.username = username;
  }

  private String getImageUrl() {
    //from Rocket.Chat:packages/rocketchat-ui/lib/avatar.coffee
    //REMARK! this is often SVG image! (see: Rocket.Chat:server/startup/avatar.coffee)
    try {
      final String avatarUrl = "/avatar/" + URLEncoder.encode(username, "UTF-8");
      if (absoluteUrl == null) {
        return avatarUrl;
      }
      return absoluteUrl.from(avatarUrl);
    } catch (UnsupportedEncodingException exception) {
      RCLog.e(exception, "failed to get URL for user: %s", username);
      return null;
    }
  }

  /**
   * render avatar into RocketChatAvatar.
   */
  public void into(final RocketChatAvatar rocketChatAvatar, boolean showFailureImage) {
    if (showFailureImage) {
      rocketChatAvatar.showFailureImage();
    } else {
      rocketChatAvatar.loadImage(getImageUrl());
    }
  }
}