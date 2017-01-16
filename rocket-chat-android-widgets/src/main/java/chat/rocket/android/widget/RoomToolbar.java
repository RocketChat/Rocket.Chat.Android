package chat.rocket.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;

import java.lang.reflect.Field;

public class RoomToolbar extends Toolbar {

  private TextView titleTextView;
  private ImageView badgeImageView;

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

  public void setUnreadBudge(int numUnreadChannels, int numMentionsSum) {
    if (getNavigationIcon() == null) {
      return;
    }

    if (badgeImageView == null) {
      badgeImageView = new AppCompatImageView(getContext());
      badgeImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    if (badgeImageView.getParent() != this) { //ref: Toolbar#isChildOrHidden
      addView(badgeImageView, generateDefaultLayoutParams());
    }

    if (numUnreadChannels > 0) {
      if (numMentionsSum > 0) {
        badgeImageView.setImageDrawable(getBadgeDrawable(numMentionsSum));
      } else {
        badgeImageView.setScaleType(ImageView.ScaleType.CENTER);
        badgeImageView.setImageResource(R.drawable.badge_without_number);
      }
      badgeImageView.setVisibility(View.VISIBLE);
    } else {
      badgeImageView.setVisibility(View.GONE);
    }
  }

  private Drawable getBadgeDrawable(int number) {
    String icon = number > 99 ? "99+" : Integer.toString(number);
    return TextDrawable.builder()
        .beginConfig()
        .useFont(Typeface.SANS_SERIF)
        .endConfig()
        .buildRound(icon, ContextCompat.getColor(getContext(), R.color.badge_color));
  }



  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if (badgeImageView != null && badgeImageView.getVisibility() != View.GONE) {
      try {
        Field field = Toolbar.class.getDeclaredField("mNavButtonView");
        field.setAccessible(true);
        ImageButton navButtonView = (ImageButton) field.get(this);
        int iconLeft = navButtonView.getLeft();
        int iconTop = navButtonView.getTop();
        int iconRight = navButtonView.getRight();
        int iconBottom = navButtonView.getBottom();

        int budgeLeft = iconLeft + (iconRight - iconLeft) * 5 / 8;
        int budgeRight = iconLeft + (iconRight - iconLeft) * 7 / 8;
        int budgeTop = iconTop + (iconBottom - iconTop) / 8;
        int budgeBottom = iconTop + (iconBottom - iconTop) * 3 / 8;
        badgeImageView.layout(budgeLeft, budgeTop, budgeRight, budgeBottom);
      } catch (Exception exception) {
        return;
      }
    }
  }

}
