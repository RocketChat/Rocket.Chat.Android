package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.widget.R;

/**
 */
public class RocketChatMessageAttachmentsLayout extends LinearLayout {
  private LayoutInflater inflater;
  private String hostname;
  private String attachmentsString;

  private String userId;
  private String token;

  public RocketChatMessageAttachmentsLayout(Context context) {
    super(context);
    initialize(context, null);
  }

  public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                            int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs) {
    inflater = LayoutInflater.from(context);
    setOrientation(VERTICAL);
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public void setCredential(String userId, String token) {
    this.userId = userId;
    this.token = token;
  }

  public void setAttachments(String attachmentsString) {
    if (this.attachmentsString != null && this.attachmentsString.equals(attachmentsString)) {
      return;
    }
    this.attachmentsString = attachmentsString;
    removeAllViews();

    try {
      JSONArray attachments = new JSONArray(attachmentsString);
      for (int i = 0; i < attachments.length(); i++) {
        JSONObject attachment = attachments.getJSONObject(i);
        appendAttachmentView(attachment);
      }
    } catch (JSONException exception) {
      return;
    }
  }

  private void appendAttachmentView(JSONObject attachmentObj) throws JSONException {
    if (attachmentObj == null) {
      return;
    }

    View attachmentView = inflater.inflate(R.layout.message_inline_attachment, this, false);

    colorizeAttachmentBar(attachmentObj, attachmentView);
    showAuthorAttachment(attachmentObj, attachmentView);
    showTitleAttachment(attachmentObj, attachmentView);
    showReferenceAttachment(attachmentObj, attachmentView);
    showImageAttachment(attachmentObj, attachmentView);
    // audio
    // video
    showFieldsAttachment(attachmentObj, attachmentView);

    addView(attachmentView);
  }

  private void colorizeAttachmentBar(JSONObject attachmentObj, View attachmentView)
      throws JSONException {
    final View attachmentStrip = attachmentView.findViewById(R.id.attachment_strip);

    final String colorString = attachmentObj.optString("color");
    if (TextUtils.isEmpty(colorString)) {
      attachmentStrip.setBackgroundResource(R.color.inline_attachment_quote_line);
      return;
    }

    try {
      attachmentStrip.setBackgroundColor(Color.parseColor(colorString));
    } catch (Exception e) {
      attachmentStrip.setBackgroundResource(R.color.inline_attachment_quote_line);
    }
  }

  private void showAuthorAttachment(JSONObject attachmentObj, View attachmentView)
      throws JSONException {
    final View authorBox = attachmentView.findViewById(R.id.author_box);
    if (attachmentObj.isNull("author_name") || attachmentObj.isNull("author_link")
        || attachmentObj.isNull("author_icon")) {
      authorBox.setVisibility(GONE);
      return;
    }

    authorBox.setVisibility(VISIBLE);

    loadImage(attachmentObj.getString("author_icon"),
        (ImageView) attachmentView.findViewById(R.id.author_icon));

    final TextView authorName = (TextView) attachmentView.findViewById(R.id.author_name);
    authorName.setText(attachmentObj.getString("author_name"));

    final String link = absolutize(attachmentObj.getString("author_link"));
    authorName.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        view.getContext().startActivity(intent);
      }
    });

    // timestamp and link - need to format time
  }

  private void showTitleAttachment(JSONObject attachmentObj, View attachmentView)
      throws JSONException {
    TextView titleView = (TextView) attachmentView.findViewById(R.id.title);
    if (attachmentObj.isNull("title")) {
      titleView.setVisibility(View.GONE);
      return;
    }

    titleView.setVisibility(View.VISIBLE);
    titleView.setText(attachmentObj.getString("title"));

    if (attachmentObj.isNull("title_link")) {
      titleView.setOnClickListener(null);
      titleView.setClickable(false);
    } else {
      final String link = absolutize(attachmentObj.getString("title_link"));
      titleView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          view.getContext().startActivity(intent);
        }
      });
      TextViewCompat.setTextAppearance(titleView,
          R.style.TextAppearance_RocketChat_MessageAttachment_Title_Link);
    }
  }

  private void showReferenceAttachment(JSONObject attachmentObj, View attachmentView)
      throws JSONException {
    final View refBox = attachmentView.findViewById(R.id.ref_box);
    if (attachmentObj.isNull("thumb_url") && attachmentObj.isNull("text")) {
      refBox.setVisibility(GONE);
      return;
    }

    refBox.setVisibility(VISIBLE);

    final ImageView thumbImage = (ImageView) refBox.findViewById(R.id.thumb);

    final String thumbUrl = attachmentObj.optString("thumb_url");
    if (TextUtils.isEmpty(thumbUrl)) {
      thumbImage.setVisibility(GONE);
    } else {
      thumbImage.setVisibility(VISIBLE);
      loadImage(thumbUrl, thumbImage);
    }

    final TextView refText = (TextView) refBox.findViewById(R.id.text);

    final String refString = attachmentObj.optString("text");
    if (TextUtils.isEmpty(refString)) {
      refText.setVisibility(GONE);
    } else {
      refText.setVisibility(VISIBLE);
      refText.setText(refString);
    }
  }

  private void showImageAttachment(JSONObject attachmentObj, View attachmentView)
      throws JSONException {
    final ImageView attachedImage = (ImageView) attachmentView.findViewById(R.id.image);
    if (attachmentObj.isNull("image_url")) {
      attachedImage.setVisibility(GONE);
      return;
    }

    attachedImage.setVisibility(VISIBLE);

    loadImage(attachmentObj.getString("image_url"), attachedImage);
  }

  private void showFieldsAttachment(JSONObject attachmentObj, View attachmentView)
      throws JSONException {
    if (attachmentObj.isNull("fields")) {
      return;
    }

    final ViewGroup attachmentContent =
        (ViewGroup) attachmentView.findViewById(R.id.attachment_content);

    final JSONArray fields = attachmentObj.getJSONArray("fields");
    for (int i = 0, size = fields.length(); i < size; i++) {
      final JSONObject fieldObject = fields.getJSONObject(i);
      if (fieldObject.isNull("title") || fieldObject.isNull("value")) {
        return;
      }
      MessageAttachmentFieldLayout fieldLayout = new MessageAttachmentFieldLayout(getContext());
      fieldLayout.setTitle(fieldObject.getString("title"));
      fieldLayout.setValue(fieldObject.getString("value"));

      attachmentContent.addView(fieldLayout);
    }
  }

  private String absolutize(String url) {
    return url.startsWith("/") ? "https://" + hostname + url : url;
  }

  private GlideUrl getGlideUrl(String url) {
    final String absoluteUrl = absolutize(url);

    try {
      if (hostname.equals(Uri.parse(absoluteUrl).getHost())) {
        return new GlideUrl(absoluteUrl, new LazyHeaders.Builder()
            .addHeader("Cookie", "rc_uid=" + userId + ";rc_token=" + token)
            .build());
      }
    } catch (Exception exception) {
    }

    return new GlideUrl(absoluteUrl);
  }

  private void loadImage(String url, ImageView imageView) {
    Glide.with(getContext())
        .load(getGlideUrl(url))
        .thumbnail(Glide.with(getContext()).load(url).dontAnimate())
        .placeholder(VectorDrawableCompat.create(getResources(), R.drawable.image_dummy, null))
        .error(VectorDrawableCompat.create(getResources(), R.drawable.image_error, null))
        .into(imageView);
  }
}
