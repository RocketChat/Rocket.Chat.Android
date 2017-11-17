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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;
import java.util.Map;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.helper.FrescoHelper;
import chat.rocket.android.widget.helper.ImageFormat;
import chat.rocket.core.models.WebContent;
import chat.rocket.core.models.WebContentHeaders;
import chat.rocket.core.models.WebContentMeta;
import chat.rocket.core.models.WebContentParsedUrl;

/**
 */
public class RocketChatMessageUrlsLayout extends LinearLayout {
  private LayoutInflater inflater;
  private List<WebContent> webContents;

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

  public void setUrls(List<WebContent> webContents, boolean autoloadImages) {
    if (this.webContents != null && this.webContents.equals(webContents)) {
      return;
    }

    this.webContents = webContents;
    removeAllViews();

    for (int i = 0, size = webContents.size(); i < size; i++) {
      appendUrlView(webContents.get(i), autoloadImages);
    }
  }

  private void appendUrlView(WebContent webContent, boolean autoloadImages) {
    final String url = webContent.getUrl();

    addMessageInlineImage(webContent, autoloadImages, url);

    // see Rocket.Chat:packages/rocketchat-oembed/client/oembedUrlWidget.coffee
    final Map<WebContentMeta.Type, WebContentMeta> webContentMetaMap = webContent.getMetaMap();
    if (webContentMetaMap == null || webContentMetaMap.size() == 0) {
      return;
    }

    final String imageURL = webContent.getMetaImage();

    WebContentParsedUrl parsedUrl = webContent.getParsedUrl();
    String host = parsedUrl != null ? parsedUrl.getHost() : null;

    View embedUrl = inflater.inflate(R.layout.message_inline_embed_url, this, false);

    ((TextView) embedUrl.findViewById(R.id.hostname)).setText(host);
    ((TextView) embedUrl.findViewById(R.id.title)).setText(webContent.getMetaTitle());
    ((TextView) embedUrl.findViewById(R.id.description)).setText(removeQuote(webContent.getMetaDescription()));

    final SimpleDraweeView image = (SimpleDraweeView) embedUrl.findViewById(R.id.image);
    if (TextUtils.isEmpty(imageURL)) {
      image.setVisibility(View.GONE);
    } else {
      FrescoHelper.INSTANCE.loadImageWithCustomization(image, imageURL);
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

  private String removeQuote(String description) {
    if (description != null) {
      if (description.startsWith("\"")) {
        description = description.substring(1);
      }
      if (description.endsWith("\"")) {
        description = description.substring(0, description.length() - 1);
      }
    }
    return description;
  }

  private void addMessageInlineImage(WebContent webContent, boolean autoloadImages, String url) {
    final WebContentHeaders webContentHeaders = webContent.getHeaders();
    String contentType = webContentHeaders != null ? webContentHeaders.getContentType() : "";

    if (contentType != null && contentType.startsWith("image/")
        && ImageFormat.SUPPORTED_LIST.contains(contentType)) {
      final View inlineImageView = inflater.inflate(R.layout.message_inline_image, this, false);
      final SimpleDraweeView
          inlineImage = (SimpleDraweeView) inlineImageView.findViewById(R.id.message_inline_image);
      final View loadView = inlineImageView.findViewById(R.id.message_inline_image_load);

      loadImage(url, inlineImage, loadView, autoloadImages);
      addView(inlineImageView);
    }
  }

  private void loadImage(final String url, final SimpleDraweeView drawee, final View load,
                         boolean autoloadImage) {
    if (autoloadImage) {
      load.setVisibility(GONE);
      FrescoHelper.INSTANCE.loadImageWithCustomization(drawee, url);
      return;
    }

    load.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        load.setVisibility(GONE);
        load.setOnClickListener(null);
        FrescoHelper.INSTANCE.loadImageWithCustomization(drawee, url);
      }
    });
  }
}
