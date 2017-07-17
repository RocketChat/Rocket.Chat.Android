package chat.rocket.android.widget.message.autocomplete.user;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.RocketChatAvatar;
import chat.rocket.android.widget.message.autocomplete.AutocompleteViewHolder;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UserViewHolder extends AutocompleteViewHolder<UserItem> {

  private final TextView titleTextView;
  private final RocketChatAvatar avatar;
  private final ImageView status;

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
      avatar.loadImage(getImageUrl(suggestion, userItem.getAbsoluteUrl()));
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
}