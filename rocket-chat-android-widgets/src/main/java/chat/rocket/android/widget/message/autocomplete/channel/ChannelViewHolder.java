package chat.rocket.android.widget.message.autocomplete.channel;

import android.view.View;
import android.widget.TextView;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.message.autocomplete.AutocompleteViewHolder;

public class ChannelViewHolder extends AutocompleteViewHolder<ChannelItem> {

  private final TextView titleTextView;
  private final TextView iconTextView;

  public ChannelViewHolder(View itemView,
                           final AutocompleteViewHolder.OnClickListener<ChannelItem> onClickListener) {
    super(itemView);

    titleTextView = (TextView) itemView.findViewById(R.id.title);
    iconTextView = (TextView) itemView.findViewById(R.id.icon);

    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (onClickListener != null) {
          onClickListener.onClick((ChannelItem) v.getTag());
        }
      }
    });
  }

  @Override
  public void bind(ChannelItem channelItem) {
    itemView.setTag(channelItem);

    if (titleTextView != null) {
      titleTextView.setText(channelItem.getSuggestion());
    }

    if (iconTextView != null) {
      iconTextView.setText(channelItem.getIcon());
    }
  }

  @Override
  public void showAsEmpty() {
    iconTextView.setVisibility(View.GONE);
    titleTextView.setText(R.string.no_channel_found);
  }
}
