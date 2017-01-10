package chat.rocket.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class CustomToolbar extends Toolbar {

  private TextView titleTextView;

  public CustomToolbar(Context context) {
    super(context);
    initialize(context, null);
  }

  public CustomToolbar(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public CustomToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  private void initialize(Context context, @Nullable AttributeSet attrs) {
    View.inflate(context, R.layout.custom_toolbar, this);

    titleTextView = (TextView) findViewById(R.id.toolbar_title);

    if (titleTextView == null) {
      return;
    }

    TypedArray typedArray = context.getTheme().obtainStyledAttributes(
        attrs,
        R.styleable.CustomToolbar,
        0, 0);

    try {
      titleTextView.setText(typedArray.getText(R.styleable.CustomToolbar_titleText));
      titleTextView
          .setTextColor(typedArray.getColor(R.styleable.CustomToolbar_titleTextColor, Color.BLACK));
      titleTextView.setCompoundDrawablePadding(
          typedArray.getLayoutDimension(R.styleable.CustomToolbar_titleDrawablePadding, 0));
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

  public void setTitleDrawableLeft(@DrawableRes int drawableResId) {
    if (titleTextView == null) {
      return;
    }

    Drawable drawable = drawableResId > 0
        ? VectorDrawableCompat.create(getResources(), drawableResId, null)
        : null;

    titleTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
  }
}
