package chat.rocket.android.layouthelper.chatroom.dialog;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.message.AbstractMessageSpec;

public class MessageSelectionViewHolder extends RecyclerView.ViewHolder {

  private ImageView messageSpecIcon;
  private TextView messageSpecTitle;

  public MessageSelectionViewHolder(View itemView) {
    super(itemView);
    messageSpecIcon = (ImageView) itemView.findViewById(R.id.message_spec_icon);
    messageSpecTitle = (TextView) itemView.findViewById(R.id.message_spec_title);
  }

  public void onBind(AbstractMessageSpec abstractMessageSpec) {
    itemView.setTag(abstractMessageSpec);

    AbstractMessageSpec.ViewData viewData = abstractMessageSpec.getViewData();
    setIconBackgroundColorTint(viewData.getBackgroundTint());
    setIcon(viewData.getIcon());
    setTitle(viewData.getTitle());
  }

  public void setIconBackgroundColorTint(@ColorRes int color) {
//    Drawable background = DrawableCompat.wrap(messageSpecIcon.getBackground());
    DrawableCompat.setTint(messageSpecIcon.getBackground(),
        ContextCompat.getColor(messageSpecIcon.getContext(), color));
  }

  public void setIcon(@DrawableRes int icon) {
    messageSpecIcon.setImageResource(icon);
  }

  public void setTitle(@StringRes int title) {
    messageSpecTitle.setText(title);
  }
}
