package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
public abstract class WebContent {

  public abstract String getUrl();

  @Nullable
  public abstract Map<WebContentMeta.Type, WebContentMeta> getMetaMap();

  @Nullable
  public abstract WebContentHeaders getHeaders();

  @Nullable
  public abstract WebContentParsedUrl getParsedUrl();

  public static Builder builder() {
    return new AutoValue_WebContent.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setUrl(String url);

    public abstract Builder setMetaMap(Map<WebContentMeta.Type, WebContentMeta> webContentMetaMap);

    public abstract Builder setHeaders(WebContentHeaders webContentHeaders);

    public abstract Builder setParsedUrl(WebContentParsedUrl webContentParsedUrl);

    public abstract WebContent build();
  }

  public String getMetaTitle() {
    final Map<WebContentMeta.Type, WebContentMeta> webContentMetaMap = getMetaMap();
    if (webContentMetaMap == null) {
      return null;
    }

    WebContentMeta webContentMeta = webContentMetaMap.get(WebContentMeta.Type.OPEN_GRAPH);
    if (webContentMeta != null && webContentMeta.getTitle() != null) {
      return webContentMeta.getTitle();
    }

    webContentMeta = webContentMetaMap.get(WebContentMeta.Type.TWITTER);
    if (webContentMeta != null && webContentMeta.getTitle() != null) {
      return webContentMeta.getTitle();
    }

    webContentMeta = webContentMetaMap.get(WebContentMeta.Type.OTHER);
    if (webContentMeta != null && webContentMeta.getTitle() != null) {
      return webContentMeta.getTitle();
    }

    return null;
  }

  public String getMetaDescription() {
    final Map<WebContentMeta.Type, WebContentMeta> webContentMetaMap = getMetaMap();
    if (webContentMetaMap == null) {
      return null;
    }

    WebContentMeta webContentMeta = webContentMetaMap.get(WebContentMeta.Type.OPEN_GRAPH);
    if (webContentMeta != null && webContentMeta.getDescription() != null) {
      return webContentMeta.getDescription();
    }

    webContentMeta = webContentMetaMap.get(WebContentMeta.Type.TWITTER);
    if (webContentMeta != null && webContentMeta.getDescription() != null) {
      return webContentMeta.getDescription();
    }

    webContentMeta = webContentMetaMap.get(WebContentMeta.Type.OTHER);
    if (webContentMeta != null && webContentMeta.getDescription() != null) {
      return webContentMeta.getDescription();
    }

    return null;
  }

  public String getMetaImage() {
    final Map<WebContentMeta.Type, WebContentMeta> webContentMetaMap = getMetaMap();
    if (webContentMetaMap == null) {
      return null;
    }

    WebContentMeta webContentMeta = webContentMetaMap.get(WebContentMeta.Type.OPEN_GRAPH);
    if (webContentMeta != null && webContentMeta.getImage() != null) {
      return webContentMeta.getImage();
    }

    webContentMeta = webContentMetaMap.get(WebContentMeta.Type.TWITTER);
    if (webContentMeta != null && webContentMeta.getImage() != null) {
      return webContentMeta.getImage();
    }

    return null;
  }
}
