package chat.rocket.android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * View for indicating "waiting for connection ..." and so on.
 */
public class WaitingView extends LinearLayout {
  private ArrayList<View> dots;

  public WaitingView(Context context) {
    super(context);
    initialize(context, null);
  }

  public WaitingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public WaitingView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public WaitingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs) {
    int size = context.getResources().getDimensionPixelSize(R.dimen.def_waiting_view_dot_size);
    int count = 3;

    if (attrs != null) {
      TypedArray array =
          context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaitingView, 0, 0);
      size = array.getDimensionPixelSize(R.styleable.WaitingView_dotSize, size);
      count = array.getInteger(R.styleable.WaitingView_dotCount, count);
      array.recycle();
    }

    dots = new ArrayList<>();
    setOrientation(HORIZONTAL);
    for (int i = 0; i < count; i++) {
      addDot(context, size);
    }

    addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
      @Override
      public void onViewAttachedToWindow(View view) {
        start();
      }

      @Override
      public void onViewDetachedFromWindow(View view) {
        cancel();
      }
    });
  }

  private void addDot(Context context, int size) {
    FrameLayout frameLayout = new FrameLayout(context);
    frameLayout.setLayoutParams(new LinearLayout.LayoutParams(size * 3 / 2, size * 3 / 2));
    ImageView dot = new ImageView(context);
    dot.setImageResource(R.drawable.normal_circle);
    dot.setLayoutParams(new FrameLayout.LayoutParams(size, size, Gravity.CENTER));
    frameLayout.addView(dot);
    addView(frameLayout);
    dots.add(dot);
  }

  private void start() {
    for (int i = 0; i < dots.size(); i++) {
      animateDot(dots.get(i), 160 * i, 480, 480);
    }
  }

  private void animateDot(final View dot, final long startDelay, final long duration,
                          final long interval) {
    dot.setScaleX(0);
    dot.setScaleY(0);
    dot.animate()
        .scaleX(1)
        .scaleY(1)
        .setDuration(duration)
        .setStartDelay(startDelay)
        .withEndAction(new Runnable() {
          @Override
          public void run() {
            dot.animate()
                .scaleX(0)
                .scaleY(0)
                .setDuration(duration)
                .setStartDelay(0)
                .withEndAction(new Runnable() {
                  @Override
                  public void run() {
                    animateDot(dot, interval, duration, interval);
                  }
                })
                .start();
          }
        })
        .start();
  }

  private void cancel() {
    for (View dot : dots) {
      dot.clearAnimation();
    }
  }
}
