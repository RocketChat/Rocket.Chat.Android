package chat.rocket.android.fragment.chatroom;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.fragment.AbstractFragment;

abstract class AbstractChatRoomFragment extends AbstractFragment {

  private static final String TABLET = "tablet";

  private TextView toolbarTitle;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    toolbarTitle = (TextView) getActivity().findViewById(R.id.toolbar_title);
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  protected void setTitleText(@StringRes int stringResId) {
    if (toolbarTitle == null) {
      return;
    }

    toolbarTitle.setText(stringResId);
  }

  protected void setTitleText(CharSequence title) {
    if (toolbarTitle == null) {
      return;
    }

    toolbarTitle.setText(title);
  }

  protected void setTitleDrawableLeft(@DrawableRes int drawableResId) {
    if (toolbarTitle == null) {
      return;
    }

    Drawable drawable = drawableResId > 0
        ? VectorDrawableCompat.create(getResources(), drawableResId, null)
        : null;

    if (drawable != null && TABLET.equals(toolbarTitle.getTag())) {
      DrawableCompat.setTint(drawable, Color.WHITE);
    }

    toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
  }
}
