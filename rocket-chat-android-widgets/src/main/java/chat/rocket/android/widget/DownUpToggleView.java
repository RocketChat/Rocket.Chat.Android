package chat.rocket.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

public class DownUpToggleView extends AppCompatCheckBox {

  public static final int RADIUS = 14;

  private boolean mShowIndicator;
  private Paint mPaint;
  private int mCx;

  public DownUpToggleView(Context context) {
    super(context);
    initialize(context, null);
  }

  public DownUpToggleView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public DownUpToggleView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs) {
    setButtonDrawable(R.drawable.down_up_toggle);
    mPaint = new Paint();
    mPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.color_accent, null));
    mPaint.setStyle(Paint.Style.FILL);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    // Pre-calculate cx so that when drawer is slid,
    // then current value of `getWidth` is not used
    mCx = getWidth() - RADIUS;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mShowIndicator) {
      canvas.drawCircle(mCx, RADIUS, RADIUS, mPaint);
    }
  }

  public void showIndicator() {
    mShowIndicator = true;
    invalidate();
  }

  public void hideIndicator() {
    mShowIndicator = false;
    invalidate();
  }
}
