package chat.rocket.android.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.Gravity;

abstract class AbstractCustomFontButton extends AppCompatButton {

  public AbstractCustomFontButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public AbstractCustomFontButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public AbstractCustomFontButton(Context context) {
    super(context);
    init();
  }

  protected abstract String getFont();

  private void init() {
    String font = getFont();
    if (font != null) {
      Typeface typeface = TypefaceHelper.getTypeface(getContext(), font);
      if (typeface != null) {
        setTypeface(typeface);
      }
    }
    setMinWidth(0);
    setMinHeight(0);
    setGravity(Gravity.CENTER);
  }
}
