package chat.rocket.android.widget.helper;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    return new CharacterStyle[]{
        new ForegroundColorSpan(ContextCompat.getColor(context, R.color.highlight_text_color)),
        new BackgroundColorSpan(
            ContextCompat.getColor(context, R.color.highlight_text_background_color)),
        new StyleSpan(Typeface.BOLD), new TypefaceSpan("monospace")
    };
  }

  private static ForegroundColorSpan createTransparentSpan() {
    return new ForegroundColorSpan(Color.TRANSPARENT);
  }

  private static final Pattern HIGHLIGHT_PATTERN = Pattern.compile(
      "(^|&gt;|[ >_*~])\\`([^`\\r\\n]+)\\`([<_*~]|\\B|\\b|$)", Pattern.MULTILINE);

  private static CharSequence highlightInner(final Context context, final CharSequence text) {
    final SpannableString inputText = new SpannableString(text);

    Matcher matcher = HIGHLIGHT_PATTERN.matcher(inputText);

    while (matcher.find()) {
      setSpan(inputText, context,
          matcher.start() + matcher.group(1).length(),
          matcher.end() - matcher.group(3).length(),
          1, 1);
    }

    return inputText;
  }

  private static void setSpan(SpannableString inputText, Context context,
                              int start, int end, int markStartLen, int markEndLen) {
    if (markStartLen > 0) {
      inputText.setSpan(createTransparentSpan(),
          start, start + markStartLen,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    CharacterStyle[] spans =
        inputText.getSpans(start + markStartLen, end - markEndLen, CharacterStyle.class);
    for (CharacterStyle span : spans) {
      inputText.removeSpan(span);
    }
    for (CharacterStyle span : createCharStyles(context)) {
      inputText.setSpan(span,
          start + markStartLen, end - markEndLen,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    if (markEndLen > 0) {
      inputText.setSpan(createTransparentSpan(),
          end - markEndLen, end,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }
}
