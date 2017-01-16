package chat.rocket.android.api.rest;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DefaultServerPolicyApi implements ServerPolicyApi {

  private static final String SECURE_PROTOCOL = "https://";
  private static final String INSECURE_PROTOCOL = "http://";
  private static final String API_INFO_PATH = "/api/info";

  private final OkHttpClient client;
  private final String host;

  public DefaultServerPolicyApi(@NonNull OkHttpClient client, @NonNull String host) {
    this.client = client;
    this.host = host;
  }

  @Override
  public void getApiInfoSecurely(@NonNull Callback callback) {
    client.newCall(createRequest(SECURE_PROTOCOL)).enqueue(getOkHttpCallback(callback));
  }

  @Override
  public void getApiInfoInsecurely(@NonNull Callback callback) {
    client.newCall(createRequest(INSECURE_PROTOCOL)).enqueue(getOkHttpCallback(callback));
  }

  private Request createRequest(@NonNull String protocol) {
    return new Request.Builder()
        .url(protocol + host + API_INFO_PATH)
        .get()
        .build();
  }

  private okhttp3.Callback getOkHttpCallback(@NonNull Callback callback) {
    return new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onNetworkError();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
          callback.onResponseError();
          return;
        }

        final ResponseBody body = response.body();
        if (body == null || body.contentLength() == 0) {
          callback.onResponseError();
          return;
        }

        try {
          callback.onSuccess(new JSONObject(body.string()));
        } catch (Exception e) {
          callback.onResponseError();
        }
      }
    };
  }
}
