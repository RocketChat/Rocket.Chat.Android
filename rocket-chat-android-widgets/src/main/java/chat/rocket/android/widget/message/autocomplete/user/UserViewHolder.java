package chat.rocket.android.widget.message.autocomplete.user;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.RocketChatAvatar;
import chat.rocket.android.widget.helper.AvatarHelper;
import chat.rocket.android.widget.message.autocomplete.AutocompleteViewHolder;

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
      String absoluteUri = AvatarHelper.INSTANCE.getAbsoluteUri(userItem.getAbsoluteUrl(), suggestion);
      Drawable placeholderDrawable = AvatarHelper.INSTANCE.getTextDrawable(suggestion, itemView.getContext());
      avatar.loadImage(absoluteUri, placeholderDrawable);
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
}