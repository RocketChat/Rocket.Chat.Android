package chat.rocket.android.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * TextView with font-awesome.
 */
public class FontAwesomeTextView extends AbstractCustomFontTextView {
  public FontAwesomeTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public FontAwesomeTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FontAwesomeTextView(Context context) {
    super(context);
  }

  @Override protected String getFont() {
    return "fontawesome-webfont.ttf";
  }
}
