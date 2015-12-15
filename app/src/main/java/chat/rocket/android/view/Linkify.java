package chat.rocket.android.view;

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

import bolts.Task;
import bolts.TaskCompletionSource;

public class Linkify {

    private static HashSet<LinkType> sTargetType = new HashSet<>();
    static {
        sTargetType.add(LinkType.URL);
        sTargetType.add(LinkType.EMAIL);
    }

    public static Task<SpannableString> markupAsync(TextView textview){
        textview.setMovementMethod(LinkMovementMethodCompat.getInstance());
        final CharSequence text = textview.getText().toString();

        final TaskCompletionSource<SpannableString> task = new TaskCompletionSource<>();
        (new Thread(){
            @Override
            public void run(){
                task.setResult(markupInner(text));
            }
        }).start();
        return task.getTask();
    }

    public static void markupSync(TextView textview){
        textview.setMovementMethod(LinkMovementMethodCompat.getInstance());
        final CharSequence text = textview.getText().toString();
        textview.setText(markupInner(text));
    }

    private static SpannableString markupInner(final CharSequence text){
        LinkExtractor linkExtractor = LinkExtractor.builder()
                .linkTypes(sTargetType)
                .build();

        SpannableString s = new SpannableString(text);
        for(LinkSpan link: linkExtractor.extractLinks(text)){
            final int idx1 = link.getBeginIndex();
            final int idx2 = link.getEndIndex();
            final String url = text.subSequence(idx1,idx2).toString();

            s.setSpan(createLinkSpan(url), idx1,idx2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return s;
    }

    private static ClickableSpan createLinkSpan(final String url){
        return new ClickableSpan() {
            @Override
            public void onClick(View view) {
                final Context context = view.getContext();
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return;
                } catch (Exception e){}

                try{
                    ClipboardManager clipboardManager =
                            (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("linkURL",url));
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                } catch (Exception e){}
            }
        };
    }
}