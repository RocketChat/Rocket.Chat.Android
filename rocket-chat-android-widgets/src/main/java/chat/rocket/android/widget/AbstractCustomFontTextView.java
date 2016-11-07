package chat.rocket.android.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

abstract class AbstractCustomFontTextView extends AppCompatTextView {

  public AbstractCustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public AbstractCustomFontTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public AbstractCustomFontTextView(Context context) {
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
  }
}
