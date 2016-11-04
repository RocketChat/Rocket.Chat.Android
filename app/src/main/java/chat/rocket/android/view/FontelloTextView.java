package chat.rocket.android.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * TextView with fontello
 */
public class FontelloTextView extends AbstractCustomFontTextView {
    public FontelloTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FontelloTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FontelloTextView(Context context) {
        super(context);
    }

    @Override
    protected String getFont() {
        return "fontello.ttf";
    }
}
