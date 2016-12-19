package chat.rocket.android.widget.helper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
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

      spannableString.setSpan(createLinkSpan(url), idx1, idx2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    return spannableString;
  }

  private static ClickableSpan createLinkSpan(final String url) {
    return new ClickableSpan() {
      @Override
      public void onClick(View view) {
        final Context context = view.getContext();
        try {
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          context.startActivity(intent);
          return;
        } catch (Exception exception) {
        }

        try {
          ClipboardManager clipboardManager =
              (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
          clipboardManager.setPrimaryClip(ClipData.newPlainText("linkURL", url));
          Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
        }
      }
    };
  }
}