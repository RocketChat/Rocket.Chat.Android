package chat.rocket.android.model.ddp;

import io.realm.RealmObject;

/**
 * Attachment.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class MessageAttachment extends RealmObject {
  private String title;
  private String title_url;
  private String image_url;
  private String image_type;
  private String image_size;
}
