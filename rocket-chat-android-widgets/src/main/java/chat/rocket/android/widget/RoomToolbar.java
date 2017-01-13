package chat.rocket.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class RoomToolbar extends Toolbar {

  private TextView titleTextView;

  public RoomToolbar(Context context) {
    super(context);
    initialize(context, null);
  }

  public RoomToolbar(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public RoomToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  private void initialize(Context context, @Nullable AttributeSet attrs) {
    View.inflate(context, R.layout.room_toolbar, this);

    titleTextView = (TextView) findViewById(R.id.toolbar_title);

    if (titleTextView == null) {
      return;
    }

    TypedArray typedArrayBase = context.getTheme().obtainStyledAttributes(new int[]{
        R.attr.titleTextAppearance
    });
    try {
      TextViewCompat.setTextAppearance(titleTextView,
          typedArrayBase.getResourceId(0,
              android.support.v7.appcompat.R.style.TextAppearance_Widget_AppCompat_Toolbar_Title));
    } finally {
      typedArrayBase.recycle();
    }

    TypedArray typedArray = context.getTheme().obtainStyledAttributes(
        attrs,
        R.styleable.RoomToolbar,
        0, 0);

    try {
      titleTextView.setText(typedArray.getText(R.styleable.RoomToolbar_titleText));
      titleTextView.setCompoundDrawablePadding(
          typedArray.getLayoutDimension(R.styleable.RoomToolbar_titleDrawablePadding, 0));
    } finally {
      typedArray.recycle();
    }
  }

  @Override
  public void setTitle(@StringRes int resId) {
    if (titleTextView != null) {
      titleTextView.setText(resId);
      return;
    }
    super.setTitle(resId);
  }

  @Override
  public void setTitle(CharSequence title) {
    if (titleTextView != null) {
      titleTextView.setText(title);
      return;
    }
    super.setTitle(title);
  }

  public void setRoomIcon(@DrawableRes int drawableResId) {
    if (titleTextView == null) {
      return;
    }

    Drawable drawable = drawableResId > 0
        ? VectorDrawableCompat.create(getResources(), drawableResId, null)
        : null;

    titleTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
  }
}
