package chat.rocket.android.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
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

import chat.rocket.android.widget.helper.DrawableHelper;

public class RoomToolbar extends Toolbar {
  private TextView toolbarText;
  private ImageView roomTypeImage;
  private ImageView userStatusImage;
  private ImageView badgeImageView;

  private Drawable privateChannelDrawable;
  private Drawable publicChannelDrawable;
  private Drawable livechatChannelDrawable;
  private Drawable userStatusDrawable;

  private DrawerArrowDrawable drawerArrowDrawable;

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
    View.inflate(context, R.layout.toolbar, this);
    setNavigationIcon(context);

    toolbarText = findViewById(R.id.text_toolbar);
    roomTypeImage = findViewById(R.id.image_room_type);
    userStatusImage = findViewById(R.id.image_user_status);

    privateChannelDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_lock_white_24dp, null);
    publicChannelDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_hashtag_white_24dp, null);
    livechatChannelDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_livechat_white_24dp, null);
    userStatusDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_user_status_black_24dp, null).mutate();
  }

  private void setNavigationIcon(Context context) {
    drawerArrowDrawable = new DrawerArrowDrawable(context);
    drawerArrowDrawable.setColor(ContextCompat.getColor(context, android.R.color.white));
    super.setNavigationIcon(drawerArrowDrawable);
  }

  public void setNavigationIconProgress(float progress) {
    drawerArrowDrawable.setProgress(progress);
  }

  public void setNavigationIconVerticalMirror(boolean verticalMirror) {
    drawerArrowDrawable.setVerticalMirror(verticalMirror);
  }

  @Override
  public void setTitle(@StringRes int resId) {
    toolbarText.setText(getContext().getText(resId));
  }

  @Override
  public void setTitle(CharSequence title) {
    toolbarText.setText(title);
  }

  public void hideChannelIcons() {
    roomTypeImage.setVisibility(GONE);
    userStatusImage.setVisibility(GONE);
  }

  public void showPrivateChannelIcon() {
    roomTypeImage.setImageDrawable(privateChannelDrawable);
    userStatusImage.setVisibility(GONE);
    roomTypeImage.setVisibility(VISIBLE);
  }

  public void showPublicChannelIcon() {
    roomTypeImage.setImageDrawable(publicChannelDrawable);
    userStatusImage.setVisibility(GONE);
    roomTypeImage.setVisibility(VISIBLE);
  }

  public void showLivechatChannelIcon() {
    roomTypeImage.setImageDrawable(livechatChannelDrawable);
    userStatusImage.setVisibility(GONE);
    roomTypeImage.setVisibility(VISIBLE);
  }

  public void showUserStatusIcon(int status) {
    DrawableHelper.INSTANCE.wrapDrawable(userStatusDrawable);

    Context context = getContext();
    switch (status) {
      case STATUS_ONLINE:
        DrawableHelper.INSTANCE.tintDrawable(userStatusDrawable, context, R.color.color_user_status_online);
        break;
      case STATUS_BUSY:
        DrawableHelper.INSTANCE.tintDrawable(userStatusDrawable, context, R.color.color_user_status_busy);
        break;
      case STATUS_AWAY:
        DrawableHelper.INSTANCE.tintDrawable(userStatusDrawable, context, R.color.color_user_status_away);
        break;
      case STATUS_OFFLINE:
        DrawableHelper.INSTANCE.tintDrawable(userStatusDrawable, context, R.color.color_user_status_offline);
        break;
      default:
        DrawableHelper.INSTANCE.tintDrawable(userStatusDrawable, context, R.color.color_user_status_offline);
        break;
    }

    userStatusImage.setImageDrawable(userStatusDrawable);
    roomTypeImage.setVisibility(GONE);
    userStatusImage.setVisibility(VISIBLE);
  }

  public void setUnreadBadge(int numUnreadChannels, int numMentionsSum) {
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
        badgeImageView.setImageResource(R.drawable.ic_badge_without_number_red_10dp);
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
        .buildRound(icon, ContextCompat.getColor(getContext(), R.color.color_alert));
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