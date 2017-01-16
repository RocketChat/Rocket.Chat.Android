package chat.rocket.android.api.rest;

import android.support.annotation.NonNull;
import org.json.JSONObject;

public interface ServerPolicyApi {

  void getApiInfoSecurely(@NonNull Callback callback);

  void getApiInfoInsecurely(@NonNull Callback callback);

  interface Callback {
    void onSuccess(JSONObject jsonObject);

    void onResponseError();

    void onNetworkError();
  }
}
