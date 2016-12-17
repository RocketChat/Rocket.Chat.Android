package chat.rocket.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * divider.
 */
public class DividerView extends FrameLayout {
  public DividerView(Context context) {
    super(context);
    initialize(context, null);
  }

  public DividerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public DividerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs) {
    int
        thickness =
        context.getResources().getDimensionPixelSize(R.dimen.def_divider_view_thickness);

    if (attrs != null) {
      TypedArray array =
          context.getTheme().obtainStyledAttributes(attrs, R.styleable.DividerView, 0, 0);
      thickness = array.getDimensionPixelSize(R.styleable.DividerView_thickness, thickness);
      array.recycle();
    }

    int color = Color.GRAY;
    TypedArray array2 = context.getTheme().obtainStyledAttributes(new int[]{
        R.attr.colorControlNormal
    });
    color = array2.getColor(0, color);
    array2.recycle();

    setBackgroundColor(color);
    setAlpha(0.4f);
    setMinimumWidth(thickness);
    setMinimumHeight(thickness);
  }
}
