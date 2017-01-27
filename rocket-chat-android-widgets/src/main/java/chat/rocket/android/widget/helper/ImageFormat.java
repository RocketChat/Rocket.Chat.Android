package chat.rocket.android.widget.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public interface ImageFormat {
  String PNG = "image/png";
  String JPG = "image/jpg";
  String JPEG = "image/jpeg";
  String WEBP = "image/webp";
  String GIF = "image/gif";
  String SVG = "image/svg+xml";

  List<String> SUPPORTED_LIST = Collections.unmodifiableList(new ArrayList<String>() {
    {
      add(PNG);
      add(JPG);
      add(JPEG);
      add(WEBP);
      add(GIF);
      add(SVG);
    }
  });
}
