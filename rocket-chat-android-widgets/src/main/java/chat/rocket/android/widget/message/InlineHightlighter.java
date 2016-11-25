package chat.rocket.android.widget.message;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.widget.TextView;
import chat.rocket.android.widget.R;

public class InlineHightlighter {
  //TODO: not completely implemented...
  // original implementation is RocketChat:packages/rocketchat-markdown/markdown.coffee
  // regex pattern with '/(^|&gt;|[ >_*~])\`([^`\r\n]+)\`([<_*~]|\B|\b|$)/gm'

  public static void highlight(TextView textview) {
    final CharSequence text = textview.getText();
    textview.setText(highlightInner(textview.getContext(), text));
  }

  private static CharacterStyle[] createCharStyles(final Context context) {
    return new CharacterStyle[] {
        new ForegroundColorSpan(ContextCompat.getColor(context, R.color.highlight_text_color)),
        new BackgroundColorSpan(
            ContextCompat.getColor(context, R.color.highlight_text_background_color)),
        new StyleSpan(Typeface.BOLD), new TypefaceSpan("monospace")
    };
  }

  private static ForegroundColorSpan createTransparentSpan() {
    return new ForegroundColorSpan(Color.TRANSPARENT);
  }

  private static CharSequence highlightInner(final Context context, final CharSequence text) {
    final SpannableString s = new SpannableString(text);

    final int length = text.length();
    int highlightStart = length;
    for (int i = 0; i < length; i++) {
      char chr = text.charAt(i);
      if (chr == '`') {
        if (i > highlightStart) {
          final int highlightEnd = i;
          if (highlightStart + 1 < highlightEnd) {
            s.setSpan(createTransparentSpan(), highlightStart, highlightStart + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            for (CharacterStyle span : createCharStyles(context)) {
              s.setSpan(span, highlightStart + 1, highlightEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            s.setSpan(createTransparentSpan(), highlightEnd, highlightEnd + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          }
          highlightStart = length;
        } else {
          highlightStart = i;
        }
      }
    }

    return s;
  }
}
