package chat.rocket.android.widget.layouthelper;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.message.MessageExtraActionItemPresenter;

public class MessageExtraActionItemViewHolder extends RecyclerView.ViewHolder {

  private ImageView iconView;
  private TextView titleView;

  public MessageExtraActionItemViewHolder(View itemView) {
    super(itemView);
    iconView = (ImageView) itemView.findViewById(R.id.icon);
    titleView = (TextView) itemView.findViewById(R.id.title);
  }

  public void onBind(MessageExtraActionItemPresenter actionItem) {
    itemView.setTag(actionItem.getItemId());

    setIconBackgroundColorTint(actionItem.getBackgroundTint());
    setIcon(actionItem.getIcon());
    setTitle(actionItem.getTitle());
  }

  public void setIconBackgroundColorTint(@ColorRes int color) {
    Drawable background = DrawableCompat.wrap(iconView.getBackground());
    DrawableCompat.setTint(background,
        ContextCompat.getColor(iconView.getContext(), color));

    iconView.setBackground(background);
  }

  public void setIcon(@DrawableRes int icon) {
    iconView.setImageResource(icon);
  }

  public void setTitle(@StringRes int title) {
    titleView.setText(title);
  }
}
