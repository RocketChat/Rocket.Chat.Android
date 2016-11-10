package chat.rocket.android.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

public class DownUpToggleView extends AppCompatCheckBox {

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
  }
}
