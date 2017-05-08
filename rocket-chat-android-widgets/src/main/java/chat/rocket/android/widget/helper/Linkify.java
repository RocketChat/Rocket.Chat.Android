package chat.rocket.android.widget.helper;

import android.text.SpannableString;
import android.text.Spanned;
import android.widget.TextView;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;

import java.util.HashSet;

public class Linkify {

  private static HashSet<LinkType> sTargetType = new HashSet<>();

  static {
    sTargetType.add(LinkType.URL);
    sTargetType.add(LinkType.EMAIL);
  }

  public static void markup(TextView textview) {
    textview.setMovementMethod(LinkMovementMethodCompat.getInstance());
    textview.setClickable(false);
    textview.setLongClickable(false);
    final CharSequence text = textview.getText();
    textview.setText(markupInner(text));
  }

  private static SpannableString markupInner(final CharSequence text) {
    LinkExtractor linkExtractor = LinkExtractor.builder().linkTypes(sTargetType).build();

    SpannableString spannableString = new SpannableString(text);
    for (LinkSpan link : linkExtractor.extractLinks(text)) {
      final int idx1 = link.getBeginIndex();
      final int idx2 = link.getEndIndex();
      final String url = text.subSequence(idx1, idx2).toString();

      spannableString.setSpan(MarkDown.createLinkSpan(url), idx1, idx2,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    return spannableString;
  }
}