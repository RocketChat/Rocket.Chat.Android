package chat.rocket.android.widget.message.autocomplete.user;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.RocketChatAvatar;
import chat.rocket.android.widget.message.autocomplete.AutocompleteViewHolder;
import com.amulyakhare.textdrawable.TextDrawable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UserViewHolder extends AutocompleteViewHolder<UserItem> {

  private final TextView titleTextView;
  private final RocketChatAvatar avatar;
  private final ImageView status;

  private static final int[] COLORS = new int[] {
      0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5, 0xFF2196F3,
      0xFF03A9F4, 0xFF00BCD4, 0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39,
      0xFFFFC107, 0xFFFF9800, 0xFFFF5722, 0xFF795548, 0xFF9E9E9E, 0xFF607D8B
  };

  public UserViewHolder(View itemView, final AutocompleteViewHolder.OnClickListener<UserItem> onClickListener) {
    super(itemView);

    titleTextView = itemView.findViewById(R.id.title);
    avatar = itemView.findViewById(R.id.avatar);
    status = itemView.findViewById(R.id.status);

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
      avatar.loadImage(getImageUrl(suggestion, userItem.getAbsoluteUrl()), getTextDrawable(itemView.getContext(), suggestion));
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
      final String avatarUrl = "/avatar/" + URLEncoder.encode(username, "UTF-8");
      //  TODO why absoluteUrl is nullable? By allowing that, the app tries to load non-existing images
      if (absoluteUrl == null) {
        return avatarUrl;
      }
      return absoluteUrl.from(avatarUrl);
    } catch (UnsupportedEncodingException exception) {
      return null;
    }
  }

  private Drawable getTextDrawable(Context context, String username) {
    int round = (int) (4 * context.getResources().getDisplayMetrics().density);

    return TextDrawable.builder()
        .beginConfig()
        .useFont(Typeface.SANS_SERIF)
        .endConfig()
        .buildRoundRect(getUsernameInitials(username), getUserAvatarBackgroundColor(username), round);
  }

  private String getUsernameInitials(String username) {
    if (username.isEmpty()) {
      return "?";
    }

    String[] splitUsername = username.split(".");
    if (splitUsername.length > 1) {
      return (splitUsername[0].substring(0, 1) + splitUsername[splitUsername.length - 1].substring(0, 1)).toUpperCase();
    } else {
      if (username.length() > 1) {
        return username.substring(0, 2).toUpperCase();
      } else {
        return username.substring(0, 1).toUpperCase();
      }
    }
  }

  private int getUserAvatarBackgroundColor(String username) {
    return COLORS[username.length() % COLORS.length];
  }
}