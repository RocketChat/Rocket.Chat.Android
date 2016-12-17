package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.helper.ImageFormat;

/**
 */
public class RocketChatMessageUrlsLayout extends LinearLayout {
  private LayoutInflater inflater;
  private String urlsString;

  public RocketChatMessageUrlsLayout(Context context) {
    super(context);
    initialize(context, null);
  }

  public RocketChatMessageUrlsLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public RocketChatMessageUrlsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RocketChatMessageUrlsLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                     int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs) {
    inflater = LayoutInflater.from(context);
    setOrientation(VERTICAL);
  }

  public void setUrls(String urlsString) {
    if (this.urlsString != null && this.urlsString.equals(urlsString)) {
      return;
    }
    this.urlsString = urlsString;
    removeAllViews();

    try {
      JSONArray urls = new JSONArray(urlsString);
      for (int i = 0; i < urls.length(); i++) {
        JSONObject url = urls.getJSONObject(i);
        appendUrlView(url);
      }
    } catch (JSONException exception) {
      return;
    }
  }

  private void appendUrlView(JSONObject urlObj) throws JSONException {
    final String url = urlObj.getString("url");
    String contentType = urlObj.getJSONObject("headers").getString("contentType");

    if (contentType.startsWith("image/") && ImageFormat.SUPPORTED_LIST.contains(contentType)) {
      View inlineImage = inflater.inflate(R.layout.message_inline_image, this, false);
      Picasso.with(getContext())
          .load(url)
          .placeholder(R.drawable.image_dummy)
          .error(R.drawable.image_error)
          .into((ImageView) inlineImage.findViewById(R.id.message_inline_image));
      addView(inlineImage);
    }

    // see Rocket.Chat:packages/rocketchat-oembed/client/oembedUrlWidget.coffee
    if (!urlObj.isNull("meta")) {
      JSONObject meta = urlObj.getJSONObject("meta");

      String title = null;
      if (!meta.isNull("ogTitle")) {
        title = meta.getString("ogTitle");
      } else if (!meta.isNull("twitterTitle")) {
        title = meta.getString("twitterTitle");
      } else if (!meta.isNull("pageTitle")) {
        title = meta.getString("pageTitle");
      }

      String description = null;
      if (!meta.isNull("ogDescription")) {
        description = meta.getString("ogDescription");
      } else if (!meta.isNull("twitterDescription")) {
        description = meta.getString("twitterDescription");
      } else if (!meta.isNull("description")) {
        description = meta.getString("description");
      }

      if (description != null) {
        if (description.startsWith("\"")) {
          description = description.substring(1);
        }
        if (description.endsWith("\"")) {
          description = description.substring(0, description.length() - 1);
        }
      }

      String imageURL = null;
      if (!meta.isNull("ogImage")) {
        imageURL = meta.getString("ogImage");
      } else if (!meta.isNull("twitterImage")) {
        imageURL = meta.getString("twitterImage");
      }

      String host = urlObj.getJSONObject("parsedUrl").getString("host");

      View embedUrl = inflater.inflate(R.layout.message_inline_embed_url, this, false);

      ((TextView) embedUrl.findViewById(R.id.hostname)).setText(host);
      ((TextView) embedUrl.findViewById(R.id.title)).setText(title);
      ((TextView) embedUrl.findViewById(R.id.description)).setText(description);

      ImageView image = (ImageView) embedUrl.findViewById(R.id.image);
      if (TextUtils.isEmpty(imageURL)) {
        image.setVisibility(View.GONE);
      } else {
        Picasso.with(getContext())
            .load(imageURL)
            .placeholder(R.drawable.image_dummy)
            .error(R.drawable.image_error)
            .into(image);
        image.setVisibility(View.VISIBLE);
      }

      embedUrl.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          getContext().startActivity(intent);
        }
      });

      addView(embedUrl);
    }
  }
}
