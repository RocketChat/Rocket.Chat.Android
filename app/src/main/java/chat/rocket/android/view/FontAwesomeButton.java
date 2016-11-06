package chat.rocket.android.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Button with font-awesome text.
 */
public class FontAwesomeButton extends AbstractCustomFontButton {
  public FontAwesomeButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public FontAwesomeButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FontAwesomeButton(Context context) {
    super(context);
  }

  @Override protected String getFont() {
    return "fontawesome-webfont.ttf";
  }
}
