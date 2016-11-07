package chat.rocket.android.renderer;

import android.content.Context;
import android.widget.TextView;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfigCredential;

public class ServerConfigCredentialRenderer extends AbstractRenderer<ServerConfigCredential> {
  public ServerConfigCredentialRenderer(Context context, ServerConfigCredential credential) {
    super(context, credential);
  }

  public ServerConfigCredentialRenderer usernameInto(TextView textView) {
    if (!shouldHandle(textView)) {
      return this;
    }

    if ("email".equals(object.getType())
        && !TextUtils.isEmpty(object.getUsername())) {
      textView.setText(object.getUsername());
    }

    return this;
  }
}
