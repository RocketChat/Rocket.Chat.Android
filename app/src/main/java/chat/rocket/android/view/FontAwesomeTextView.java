package chat.rocket.android.view;

import android.content.Context;
import android.util.AttributeSet;

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

    @Override
    protected String getFont() {
        return "fontawesome-webfont.ttf";
    }
}
