package chat.rocket.android.widget.message.autocomplete.user;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.RocketChatAvatar;
import chat.rocket.android.widget.message.autocomplete.AutocompleteViewHolder;

public class UserViewHolder extends AutocompleteViewHolder<UserItem> {

  private static final int[] COLORS = new int[]{
      0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5, 0xFF2196F3,
      0xFF03A9F4, 0xFF00BCD4, 0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39,
      0xFFFFC107, 0xFFFF9800, 0xFFFF5722, 0xFF795548, 0xFF9E9E9E, 0xFF607D8B
  };

  private final TextView titleTextView;
  private final RocketChatAvatar avatar;
  private final ImageView status;

  public UserViewHolder(View itemView,
                        final AutocompleteViewHolder.OnClickListener<UserItem> onClickListener) {
    super(itemView);

    titleTextView = (TextView) itemView.findViewById(R.id.title);
    avatar = (RocketChatAvatar) itemView.findViewById(R.id.avatar);
    status = (ImageView) itemView.findViewById(R.id.status);

    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (onClickListener != null) {
          onClickListener.onClick((UserItem) v.getTag());
        }
      }
    });
  }

  @Override
  public void bind(UserItem userItem) {
    itemView.setTag(userItem);

    final String suggestion = userItem.getSuggestion();

    if (titleTextView != null) {
      titleTextView.setText(suggestion);
    }

    if (avatar != null) {
      avatar.loadImage(
          getImageUrl(suggestion, userItem.getAbsoluteUrl()),
          getTextDrawable(itemView.getContext(), suggestion)
      );
    }

    if (status != null) {
      status.setImageResource(userItem.getStatusResId());
    }
  }

  @Override
  public void showAsEmpty() {
    status.setVisibility(View.GONE);
    avatar.setVisibility(View.GONE);
    titleTextView.setText(R.string.no_user_found);
  }

  private String getImageUrl(String username, AbsoluteUrl absoluteUrl) {
    //from Rocket.Chat:packages/rocketchat-ui/lib/avatar.coffee
    //REMARK! this is often SVG image! (see: Rocket.Chat:server/startup/avatar.coffee)
    try {
      final String avatarUrl = "/avatar/" + URLEncoder.encode(username, "UTF-8") + ".jpg";
      if (absoluteUrl == null) {
        return avatarUrl;
      }
      return absoluteUrl.from(avatarUrl);
    } catch (UnsupportedEncodingException exception) {
      return null;
    }
  }

  private Drawable getTextDrawable(Context context, String username) {
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

  private int getColorForUser(String username) {
    return COLORS[username.length() % COLORS.length];
  }

  private String getInitialsForUser(String username) {
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
}
