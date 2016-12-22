package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.helper.ImageFormat;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 */
public class RocketChatMessageAttachmentsLayout extends LinearLayout {
  private LayoutInflater inflater;
  private String hostname;
  private String attachmentsString;

  private String userId;
  private String token;
  private OkHttp3Downloader downloader;

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

  private OkHttp3Downloader getDownloader() {
    if (downloader == null) {
      Interceptor interceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
          // uid/token is required to download attachment files.
          // see: RocketChat:lib/fileUpload.coffee
          Request newRequest = chain.request().newBuilder()
              .header("Cookie", "rc_uid=" + userId + ";rc_token=" + token)
              .build();
          return chain.proceed(newRequest);
        }
      };
      OkHttpClient okHttpClient = new OkHttpClient.Builder()
          .addInterceptor(interceptor)
          .build();
      downloader = new OkHttp3Downloader(okHttpClient);
    }
    return downloader;
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
    if (attachmentObj.isNull("image_url")) {
      return;
    }

    String imageURL = attachmentObj.getString("image_url");
    String imageType = attachmentObj.getString("image_type");

    if (TextUtils.isEmpty(imageURL)
        || !imageType.startsWith("image/")
        || !ImageFormat.SUPPORTED_LIST.contains(imageType)) {
      return;
    }

    View attachmentView = inflater.inflate(R.layout.message_inline_attachment, this, false);

    new Picasso.Builder(getContext())
        .downloader(getDownloader())
        .build()
        .load(absolutize(imageURL))
        .placeholder(VectorDrawableCompat.create(getResources(), R.drawable.image_dummy, null))
        .error(VectorDrawableCompat.create(getResources(), R.drawable.image_error, null))
        .into((ImageView) attachmentView.findViewById(R.id.image));

    TextView titleView = (TextView) attachmentView.findViewById(R.id.title);
    if (attachmentObj.isNull("title")) {
      titleView.setVisibility(View.GONE);
    } else {
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
      }
    }

    addView(attachmentView);
  }

  private String absolutize(String url) {
    return url.startsWith("/") ? "https://" + hostname + url : url;
  }
}
