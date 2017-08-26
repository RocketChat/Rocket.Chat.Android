package chat.rocket.android.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;

import java.lang.reflect.Field;

public class RoomToolbar extends Toolbar {
  private  TextView titleTextView;
  private ImageView roomIconImageView;
  private ImageView badgeImageView;
  Drawable privateChannelDrawable;
  Drawable publicChannelDrawable;
  Drawable userStatusDrawable;
  public static final int STATUS_ONLINE = 1;
  public static final int STATUS_BUSY = 2;
  public static final int STATUS_AWAY = 3;
  public static final int STATUS_OFFLINE = 4;

  public RoomToolbar(Context context) {
    super(context);
    initialize(context);
  }

  public RoomToolbar(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initialize(context);
  }

  public RoomToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context);
  }

  private void initialize(Context context) {
    View.inflate(context, R.layout.room_toolbar, this);
    setNavigationIcon();

    titleTextView = findViewById(R.id.toolbar_title);
    roomIconImageView = findViewById(R.id.roomIconImageView);
    userStatusDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_user_status_black_24dp, null);
    privateChannelDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_lock_black_24dp, null);
    publicChannelDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_hashtag_black_24dp, null);

    // TODO Change to lambda and method reference (AS 3+, jackOptions ?).
    // List<Drawable> drawableArrayList = Arrays.asList(userStatusDrawable, privateChannelDrawable, publicChannelDrawable);
    // drawableArrayList.forEach(...)
    // That is beautiful, but consumes more resources, does more process?? #thinking...
    wrapDrawable(userStatusDrawable);
    wrapDrawable(privateChannelDrawable);
    wrapDrawable(publicChannelDrawable);

    tintDrawable(userStatusDrawable, android.R.color.white);
    tintDrawable(privateChannelDrawable, android.R.color.white);
    tintDrawable(publicChannelDrawable, android.R.color.white);
  }

  private void setNavigationIcon() {
    Drawable menuDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_menu_black_24dp, null);
    wrapDrawable(menuDrawable);
    tintDrawable(menuDrawable, android.R.color.white);
    super.setNavigationIcon(menuDrawable);
  }

  @Override
  public void setTitle(@StringRes int resId) {
    titleTextView.setText(getContext().getText(resId));
  }

  @Override
  public void setTitle(CharSequence title) {
    titleTextView.setText(title);
  }

  public void showPrivateChannelIcon() {
    roomIconImageView.setImageDrawable(privateChannelDrawable);
  }

  public void showPublicChannelIcon() {
    roomIconImageView.setImageDrawable(publicChannelDrawable);
  }

  public void showUserStatusIcon(int status) {
    wrapDrawable(userStatusDrawable);

    switch (status) {
      case STATUS_ONLINE:
        tintDrawable(userStatusDrawable, R.color.color_user_status_online);
        break;
      case STATUS_BUSY:
        tintDrawable(userStatusDrawable, R.color.color_user_status_busy);
        break;
      case STATUS_AWAY:
        tintDrawable(userStatusDrawable, R.color.color_user_status_away);
        break;
      case STATUS_OFFLINE:
        tintDrawable(userStatusDrawable, R.color.color_user_status_offline);
        break;
      default:
        tintDrawable(userStatusDrawable, R.color.color_user_status_offline);
        break;
    }

    roomIconImageView.setImageDrawable(userStatusDrawable);
  }

  private void wrapDrawable(Drawable drawable) {
    DrawableCompat.wrap(drawable);
  }

  /**
   * REMARK: You MUST always wrap the drawable before tint it.
   * @param drawable The drawable to tint.
   * @param color The color to tint the drawable.
   * @see #wrapDrawable(Drawable)
   */
  private void tintDrawable(Drawable drawable, int color) {
    DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), color));
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
        .buildRound(icon, ContextCompat.getColor(getContext(), android.R.color.white));
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

        // put badge image at right-top side on the NavButtonView,
        // with 1/8 margin and 1/4 scale.
        int badgeLeft = iconLeft + (iconRight - iconLeft) * 5 / 8;
        int badgeRight = iconLeft + (iconRight - iconLeft) * 7 / 8;
        int badgeTop = iconTop + (iconBottom - iconTop) / 8;
        int badgeBottom = iconTop + (iconBottom - iconTop) * 3 / 8;
        badgeImageView.layout(badgeLeft, badgeTop, badgeRight, badgeBottom);
      } catch (NoSuchFieldException noSuchFieldException) {
        Log.v("RoomToolbar exception: ", noSuchFieldException.getMessage());
      } catch (IllegalAccessException illegalAccessException) {
        Log.v("RoomToolbar exception: ", illegalAccessException.getMessage());
      }
    }
  }
}
