package chat.rocket.android.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.amulyakhare.textdrawable.TextDrawable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.RocketChatAvatar;

/**
 * Helper for rendering user avatar image.
 */
public class Avatar {
  private static final int[] COLORS = new int[]{
      0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5, 0xFF2196F3,
      0xFF03A9F4, 0xFF00BCD4, 0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39,
      0xFFFFC107, 0xFFFF9800, 0xFFFF5722, 0xFF795548, 0xFF9E9E9E, 0xFF607D8B
  };
  private final AbsoluteUrl absoluteUrl;
  private final String username;

  public Avatar(AbsoluteUrl absoluteUrl, String username) {
    this.absoluteUrl = absoluteUrl;
    this.username = username;
  }

  private static int getColorForUser(String username) {
    return COLORS[username.length() % COLORS.length];
  }

  private static String getInitialsForUser(String username) {
    String name = username
        .replaceAll("[^A-Za-z0-9]", ".")
        .replaceAll("\\.+", ".")
        .replaceAll("(^\\.)|(\\.$)", "");
    String[] initials = name.split("\\.");
    if (initials.length >= 2) {
      return (firstChar(initials[0]) + firstChar(initials[initials.length - 1])).toUpperCase();
    } else {
      String name2 = name.replaceAll("[^A-Za-z0-9]", "");
      return (name2.length() < 2) ? name2 : name2.substring(0, 2).toUpperCase();
    }
  }

  private static String firstChar(String str) {
    return TextUtils.isEmpty(str) ? "" : str.substring(0, 1);
  }

  private static Bitmap drawableToBitmap(Drawable drawable, int size) {
    if (drawable instanceof BitmapDrawable) {
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      if (bitmapDrawable.getBitmap() != null) {
        return bitmapDrawable.getBitmap();
      }
    }

    Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

  private String getImageUrl() {
    //from Rocket.Chat:packages/rocketchat-ui/lib/avatar.coffee
    //REMARK! this is often SVG image! (see: Rocket.Chat:server/startup/avatar.coffee)
    try {
      final String avatarUrl = "/avatar/" + URLEncoder.encode(username, "UTF-8") + ".jpg";
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
  public void into(final RocketChatAvatar rocketChatAvatar) {
    final Context context = rocketChatAvatar.getContext();
    rocketChatAvatar.loadImage(getImageUrl(), getTextDrawable(context));
  }

  public Drawable getTextDrawable(Context context) {
    if (username == null) {
      return null;
    }

    int round = (int) (4 * context.getResources().getDisplayMetrics().density);

    return TextDrawable.builder()
        .beginConfig()
        .useFont(Typeface.SANS_SERIF)
        .endConfig()
        .buildRoundRect(getInitialsForUser(username), getColorForUser(username), round);
  }

}
